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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.emptyList;

@Value
@EqualsAndHashCode(callSuper = false)
public class ApplyCodemod extends NodeBasedRecipe {
    @Option(displayName = "Codemod transform",
            description = "Transform to be applied using the executable.",
            example = "-t path/to/transform/optimus-prime"
    )
    @Nullable
    String transform;

    @Option(displayName = "Codemod executable",
            description = "Path to the codemod executable relative to the NPM directory. Defaults to `jscodeshift/bin/jscodeshift.js`.",
            example = "@next/codemod/bin/next-codemod.js",
            required = false)
    @Nullable
    String executable;


    @Option(displayName = "File filter",
            description = "Optional glob pattern to filter files to apply the codemod to. Defaults to all files. Note: not all codemods support file glob filtering.",
            example = "**/*.(j|t)sx"
    )
    @Nullable
    String fileFilter;

    @Option(displayName = "Codemod command arguments",
            description = "Arguments which get passed to the codemod command.",
            example = "--force --jscodeshift='--parser=${parser}'",
            required = false)
    @Nullable
    List<String> codemodArgs;

    @Override
    public String getDisplayName() {
        return "Applies a codemod to all source files";
    }

    @Override
    public String getDescription() {
        return "Applies a codemod represented by an NPM package to all source files.";
    }

    @Override
    protected List<String> getNpmCommand(Accumulator acc, ExecutionContext ctx) {
        List<String> command = new ArrayList<>();
        command.add("node");

        String exec;
        if (executable == null) {
            exec = "${nodeModules}/.bin/jscodeshift -t";
        } else {
            exec = "${nodeModules}/" + executable;
        }

        String template = "${exec} ${nodeModules}/${transform} ${repoDir}${fileFilter} ${codemodArgs}";
        template = template.replace("${exec}", exec);
        template = template.replace("${transform}", Objects.requireNonNull(transform));

        for (String part : template.split(" ")) {
            part = part.trim();
            part = part.replace("${fileFilter}", fileFilter != null ? "/" + fileFilter : "");
            int argsIdx = part.indexOf("${codemodArgs}");
            if (argsIdx != -1) {
                String prefix = part.substring(0, argsIdx);
                if (!prefix.isEmpty()) {
                    command.add(prefix);
                }
                command.addAll(Optional.ofNullable(codemodArgs).orElse(emptyList()));
                String suffix = part.substring(argsIdx + "${codemodArgs}".length());
                if (!suffix.isEmpty()) {
                    command.add(suffix);
                }
            } else {
                command.add(part);
            }
        }
        return command;
    }
}
