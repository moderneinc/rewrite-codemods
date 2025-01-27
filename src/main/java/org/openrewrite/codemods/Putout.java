/*
 * Copyright 2025 the original author or authors.
 *
 * Moderne Proprietary. Only for use by Moderne customers under the terms of a commercial contract.
 */
package org.openrewrite.codemods;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.scheduling.WorkingDirectoryExecutionContextView;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Value
@EqualsAndHashCode(callSuper = false)
public class Putout extends NodeBasedRecipe {

    private static final String PUTOUT_DIR = Putout.class.getName() + ".PUTOUT_DIR";

    @Override
    public String getDisplayName() {
        return "Run Putout";
    }

    @Override
    public String getDescription() {
        return "Run [Putout](https://github.com/coderaiser/putout) on your projects.";
    }

    @Option(displayName = "Rules",
            description = "Names of rules to enable. If not provided, putout's default rules are used.",
            example = "remove-unused-variables",
            required = false)
    @Nullable
    Set<String> rules;

    @Option(displayName = "Printer",
            description = "By default Putout uses its own [putout](https://github.com/coderaiser/putout?tab=readme-ov-file#printer) printer for formatting code. You can choose an alternative printer.",
            valid = {"putout", "recast", "babel"},
            required = false)
    @Nullable
    String printer;

    @Override
    public Accumulator getInitialValue(ExecutionContext ctx) {
        Path path = RecipeResources.from(getClass()).extractResources("config", PUTOUT_DIR, ctx);
        ctx.putMessage(PUTOUT_DIR, path);
        return super.getInitialValue(ctx);
    }

    @Override
    protected List<String> getNpmCommand(Accumulator acc, ExecutionContext ctx) {
        List<String> commands = new ArrayList<>();
        String executable = "${nodeModules}/.bin/putout";

        if (rules != null) {
            commands.add(executable + " ${repoDir} --disable-all || true"); // hacky because putout throws

            // enable only rules that are provided
            for (String rule : rules) {
                commands.add(executable + " ${repoDir} --enable " + rule + " || true");
            }
        }

        commands.add(executable + " ${repoDir}" + " --disable no-html-link-for-pages || true");

        if (printer != null) {
            commands.add("node " + Objects.requireNonNull(ctx.getMessage(PUTOUT_DIR)) + "/putout.js " + printer);
        }

        commands.add(executable + " ${repoDir}" + " --fix || true");
        return commands;
    }

    @Override
    protected void runNode(Accumulator acc, ExecutionContext ctx) {
        Path dir = acc.getDirectory();
        Path nodeModules = RecipeResources.from(getClass()).init(ctx);

        List<String> commandList = getNpmCommand(acc, ctx);
        if (commandList.isEmpty()) {
            return;
        }

        Map<String, String> env = getCommandEnvironment(acc, ctx);

        // Replace placeholders in commands
        List<String> processedCommands = new ArrayList<>();
        for (String cmd : commandList) {
            processedCommands.add(cmd
                    .replace("${nodeModules}", nodeModules.toString())
                    .replace("${repoDir}", ".")
                    .replace("${parser}", acc.parser()));

        }

        Path out = null, err = null;
        try {
            for (String cmd : processedCommands) {
                List<String> singleCommand = Arrays.asList("/bin/bash", "-c", cmd);

                ProcessBuilder builder = new ProcessBuilder(singleCommand);
                builder.directory(dir.toFile());
                builder.environment().put("NODE_PATH", nodeModules.toString());
                builder.environment().put("TERM", "dumb");
                env.forEach(builder.environment()::put);

                out = Files.createTempFile(WorkingDirectoryExecutionContextView.view(ctx).getWorkingDirectory(), "node", null);
                err = Files.createTempFile(WorkingDirectoryExecutionContextView.view(ctx).getWorkingDirectory(), "node", null);
                builder.redirectOutput(ProcessBuilder.Redirect.to(out.toFile()));
                builder.redirectError(ProcessBuilder.Redirect.to(err.toFile()));

                Process process = builder.start();
                process.waitFor(5, TimeUnit.MINUTES);
                if (process.exitValue() != 0) {
                    String error = "Command failed: " + cmd;
                    if (Files.exists(err)) {
                        error += "\n" + new String(Files.readAllBytes(err));
                    }
                    throw new RuntimeException(error);
                } else {
                    for (Map.Entry<Path, Long> entry : acc.beforeModificationTimestamps.entrySet()) {
                        Path path = entry.getKey();
                        if (!Files.exists(path) || Files.getLastModifiedTime(path).toMillis() > entry.getValue()) {
                            acc.modified(path);
                        }
                    }
                    processOutput(out, acc, ctx);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (out != null) {
                //noinspection ResultOfMethodCallIgnored
                out.toFile().delete();
            }
            if (err != null) {
                //noinspection ResultOfMethodCallIgnored
                err.toFile().delete();
            }
        }
    }

    // TODO: support configuration files
//    @Override
//    protected Map<String, String> getCommandEnvironment(Accumulator acc, ExecutionContext ctx) {
//        if (configFile != null) {
//            try {
//                Path directory = WorkingDirectoryExecutionContextView.view(ctx).getWorkingDirectory();
//                Path configFilePath = Files.createTempFile(directory, "putout-config", ".json");
//                Files.write(configFilePath, this.configFile.getBytes(StandardCharsets.UTF_8));
//                if (Files.exists(configFilePath)) {
//                    return new HashMap<String, String>() {{
//                        put("PUTOUT_CONFIG_FILE", configFilePath.toString());
//                    }};
//                } else {
//                    throw new RuntimeException("Configuration file not found: " + configFilePath.toString());
//                }
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
//        return new HashMap<>();
//    }

}
