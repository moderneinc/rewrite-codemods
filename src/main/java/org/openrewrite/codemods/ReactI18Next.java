/*
 * Copyright 2025 the original author or authors.
 * <p>
 * Licensed under the Moderne Source Available License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://docs.moderne.io/licensing/moderne-source-available-license
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.codemods;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Incubating;
import org.openrewrite.Option;
import org.openrewrite.SourceFile;
import org.openrewrite.text.PlainText;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.emptyList;

@Value
@EqualsAndHashCode(callSuper = false)
@Incubating(since = "0.20.0")
public class ReactI18Next extends NodeBasedRecipe {

    @Option(displayName = "Translation file path",
            description = "Path to the translation JSON file where extracted strings will be stored. Defaults to `translations.json` in the root of the project.",
            example = "./src/locales/en.json",
            required = false)
    @Nullable
    String translationFilePath;

    @Option(displayName = "Import name",
            description = "The package name to import translation functions from. Required.",
            example = "react-i18next")
    String importName;

    @Option(displayName = "Translation root key",
            description = "Root key in the translation file to organize translations under. Defaults to `common`.",
            example = "common",
            required = false)
    @Nullable
    String translationRoot;

    @Option(displayName = "File pattern",
            description = "Glob pattern to specify which files to transform. Defaults to all files.",
            example = "src/**/*.tsx",
            required = false)
    @Nullable
    String filePattern;

    @Option(displayName = "Parser",
            description = "Parser to use for transforming files. Defaults to auto-detection based on file extensions (`tsx`/`ts`/`babel`).",
            example = "tsx",
            required = false)
    @Nullable
    String parser;

    @Override
    public String getDisplayName() {
        return "React i18next internationalization";
    }

    @Override
    public String getDescription() {
        return "Automatically internationalizes React applications by extracting hardcoded strings " +
                "and replacing them with [react-i18next](https://react.i18next.com) translation calls. " +
                "Handles JSX text, attributes, and template literals with variables. " +
                "Creates and updates a translation JSON file with extracted strings.";
    }


    @Override
    protected List<String> getNpmCommand(Accumulator acc, ExecutionContext ctx) {
        String translationPath = translationFilePath != null ? translationFilePath : "translations.json";
        String rootKey = translationRoot != null ? translationRoot : "common";

        // Ensure translation file exists in temp directory before running jscodeshift
        createDefaultTranslationsFile(acc, translationPath);

        List<String> command = new ArrayList<>();
        command.add("node");
        command.add("${nodeModules}/jscodeshift/bin/jscodeshift.js");
        command.add("-t");
        command.add("${nodeModules}/jscodeshift-react-i18next/transform.ts");

        // Add file pattern or default to all files
        if (filePattern != null) {
            command.add("${repoDir}/" + filePattern);
        } else {
            command.add("${repoDir}");
        }

        // Use custom parser or auto-detected parser
        if (parser != null) {
            command.add("--parser=" + parser);
        } else {
            command.add("--parser=${parser}");
        }

        // Required parameters
        command.add("--translationFilePath=" + translationPath);
        command.add("--importName=" + importName);

        // Always add translation root (default to "common")
        command.add("--translationRoot=" + rootKey);

        return command;
    }

    @Override
    public Collection<? extends SourceFile> generate(Accumulator acc, ExecutionContext ctx) {
        // Run the parent generate method first
        Collection<? extends SourceFile> result = super.generate(acc, ctx);

        // Handle the generated translation file
        String translationPath = translationFilePath != null ? translationFilePath : "translations.json";
        Path translationFile = acc.getDirectory().resolve(translationPath);

        if (Files.exists(translationFile)) {
            try {
                // Create a PlainText source file for the translations.json
                String content = new String(Files.readAllBytes(translationFile), StandardCharsets.UTF_8);
                PlainText translationSource = new PlainText(
                    org.openrewrite.Tree.randomId(),
                    java.nio.file.Paths.get(translationPath),
                    org.openrewrite.marker.Markers.EMPTY,
                    null,
                    false,
                    null,
                    null,
                    content,
                    emptyList()
                );

                // Return the translation file as a generated source
                List<SourceFile> sources = new ArrayList<>();
                if (result != null) {
                    sources.addAll((Collection<SourceFile>) result);
                }
                sources.add(translationSource);
                return sources;

            } catch (IOException e) {
                throw new RuntimeException("Failed to read generated translation file: " + translationPath, e);
            }
        }

        return result;
    }

    private void createDefaultTranslationsFile(Accumulator acc, String translationPath) {
        try {
            Path translationsFile = acc.getDirectory().resolve(translationPath);
            if (!Files.exists(translationsFile)) {
                // Create parent directories if they don't exist (only if not root level)
                if (translationsFile.getParent() != null && !Files.exists(translationsFile.getParent())) {
                    Files.createDirectories(translationsFile.getParent());
                }
                // Start with empty JSON object - the codemod will populate it
                String defaultTranslations = "{}";
                Files.write(translationsFile, defaultTranslations.getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create default translations file: " + translationPath, e);
        }
    }

}