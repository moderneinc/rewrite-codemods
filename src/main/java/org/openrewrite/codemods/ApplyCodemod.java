/*
 * Copyright 2023 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.codemods;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.*;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.quark.Quark;
import org.openrewrite.text.PlainText;
import org.openrewrite.tree.ParseError;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.util.Collections.emptyList;

@Value
@EqualsAndHashCode(callSuper = true)
public class ApplyCodemod extends ScanningRecipe<ApplyCodemod.Accumulator> {
    @Option(displayName = "NPM package containing the codemod",
            description = "The codemod's NPM package name.",
            example = "@next/codemod")
    String npmPackage;

    @Option(displayName = "Codemod NPM package version",
            description = "The codemod's NPM package version (defaults to `latest`).",
            example = "14.0.3",
            required = false)
    @Nullable
    String npmPackageVersion;

    @Option(displayName = "Codemod command arguments",
            description = "Arguments which get passed to the codemod command.",
            example = "built-in-next-font, ${repoDir}, --force",
            required = false)
    @Nullable
    List<String> codemodArgs;

    @Option(displayName = "Codemod command template",
            description = "Template for the command to execute (defaults to `${codemodArgs}`).",
            example = "${codemodArgs}",
            required = false)
    @Nullable
    String codemodCommandTemplate;

    @Override
    public String getDisplayName() {
        return "Applies a codemod to all source files";
    }

    @Override
    public String getDescription() {
        return "Applies a codemod represented by an NPM package to all source files.";
    }

    @Override
    public Accumulator getInitialValue(ExecutionContext ctx) {
        try {
            return new Accumulator(Files.createTempDirectory("openrewrite-codemod"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getScanner(Accumulator acc) {
        return new TreeVisitor<Tree, ExecutionContext>() {
            @Override
            public @Nullable Tree visit(@Nullable Tree tree, ExecutionContext ctx) {
                if (tree instanceof SourceFile && !(tree instanceof Quark) && !(tree instanceof ParseError)) {
                    acc.writeSource((SourceFile) tree);
                }
                return tree;
            }
        };
    }

    @Override
    public Collection<? extends SourceFile> generate(Accumulator acc, ExecutionContext ctx) {
        String template = Optional.ofNullable(codemodCommandTemplate).orElse("${codemodArgs}");
        List<String> command = new ArrayList<>();
        command.add("node");
        // FIXME extract from jar
        Path nodeModules = Paths.get("node_modules").toAbsolutePath();
        // FIXME parse `bin` from `@next/codemod/package.json`
        command.add(nodeModules.resolve(npmPackage).toString());
        if (npmPackageVersion != null) {
            command.add("--version");
            command.add(npmPackageVersion);
        }
        command.add(nodeModules.resolve("@next/codemod/bin/next-codemod.js").toString());

        for (String part : template.split(" ")) {
            part = part.trim();
            int argsIdx = part.indexOf("${codemodArgs}");
            if (argsIdx != -1) {
                String prefix = part.substring(0, argsIdx);
                if (!prefix.isEmpty()) {
                    command.add(prefix);
                }
                for (String arg : Optional.ofNullable(codemodArgs).orElse(emptyList())) {
                    arg = arg.replace("${repoDir}", ".");
                    command.add(arg);
                }
                String suffix = part.substring(argsIdx + "${codemodArgs}".length());
                if (!suffix.isEmpty()) {
                    command.add(suffix);
                }
            } else {
                command.add(part);
            }
        }

        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.command(command);
            builder.directory(acc.getDirectory().toFile());
            // FIXME do something more meaningful with the output
            builder.redirectOutput(new File("/tmp/out.txt"));
            builder.redirectError(new File("/tmp/err.txt"));
            Process process = builder.start();
            process.waitFor();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // FIXME check for generated files
        return emptyList();
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor(Accumulator acc) {
        return new TreeVisitor<Tree, ExecutionContext>() {
            @Override
            public @Nullable Tree visit(@Nullable Tree tree, ExecutionContext ctx) {
                if (tree instanceof SourceFile) {
                    SourceFile sourceFile = (SourceFile) tree;
                    if (acc.wasModified(sourceFile)) {
                        return new PlainText(
                                tree.getId(),
                                sourceFile.getSourcePath(),
                                sourceFile.getMarkers(),
                                sourceFile.getCharset() != null ? sourceFile.getCharset().name() : null,
                                sourceFile.isCharsetBomMarked(),
                                sourceFile.getFileAttributes(),
                                null,
                                acc.content(sourceFile),
                                emptyList()
                        );
                    }
                }
                return tree;
            }
        };
    }

    @Data
    public static class Accumulator {
        final Path directory;
        final Map<Path, Long> modificationTimestamps = new HashMap<>();

        public void writeSource(SourceFile tree) {
            try {
                Path path = resolvedPath(tree);
                Files.createDirectories(path.getParent());
                Path written = Files.write(path, tree.printAllAsBytes());
                modificationTimestamps.put(written, Files.getLastModifiedTime(written).toMillis());
                // TODO instead use life cycle hook or dedicated directory provided by recipe scheduler
                written.toFile().deleteOnExit();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        public boolean wasModified(SourceFile tree) {
            Path path = resolvedPath(tree);
            Long before = modificationTimestamps.get(path);
            try {
                if (before == null) return false;
                return Files.getLastModifiedTime(path).toMillis() > before;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        public String content(SourceFile tree) {
            try {
                Path path = resolvedPath(tree);
                return tree.getCharset() != null ? new String(Files.readAllBytes(path), tree.getCharset()) :
                        new String(Files.readAllBytes(path));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        private Path resolvedPath(SourceFile tree) {
            return directory.resolve(tree.getSourcePath());
        }
    }
}
