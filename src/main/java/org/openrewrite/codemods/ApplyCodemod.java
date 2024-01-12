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

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.internal.lang.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;

@Value
@EqualsAndHashCode(callSuper = true)
public class ApplyCodemod extends AbstractNodeBasedRecipe {

    @Option(displayName = "NPM package containing the codemod",
            description = "The codemod's NPM package name.",
            example = "@next/codemod",
            required = false)
    @Nullable
    String npmPackage;

    @Option(displayName = "Codemod NPM package version",
            description = "The codemod's NPM package version (defaults to `latest`).",
            example = "14.0.3",
            required = false)
    @Nullable
    String npmPackageVersion;

    @Option(displayName = "Codemod transform",
            description = "Transform to be applied using `jscodeshift`.",
            example = "built-in-next-font",
            required = false)
    @Nullable
    String transform;

    @Option(displayName = "Codemod command arguments",
            description = "Arguments which get passed to the codemod command.",
            example = "built-in-next-font, ${repoDir}, --force",
            required = false)
    @Nullable
    List<String> codemodArgs;

    @Option(displayName = "Codemod command template",
            description = "Template for the command to execute (defaults to `${nodeModules}/.bin/jscodeshift -t ${nodeModules}/${npmPackage}/transforms/${transform} ${repoDir} ${codemodArgs}`).",
            example = "${nodeModules}/.bin/jscodeshift -t ${nodeModules}/${npmPackage}/transforms/${transform} ${repoDir} ${codemodArgs}",
            required = false)
    @Nullable
    String commandTemplate;

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
        String template = Optional.ofNullable(commandTemplate).orElse("${nodeModules}/.bin/jscodeshift -t ${nodeModules}/${npmPackage}/transforms/${transform} ${repoDir} ${codemodArgs}");
        for (String part : template.split(" ")) {
            part = part.trim();
            if (npmPackage != null) {
                part = part.replace("${npmPackage}", npmPackage);
            }
            if (transform != null) {
                part = part.replace("${transform}", transform);
            }
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
