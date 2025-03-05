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

import org.openrewrite.ExecutionContext;

import java.util.Arrays;
import java.util.List;

public class Biome extends NodeBasedRecipe {
    @Override
    public String getDisplayName() {
        return "Biome recommendations";
    }

    @Override
    public String getDescription() {
        return "Run [Biome](https://biomejs.dev/) recommended settings on your projects.";
    }


    @Override
    protected List<String> getNpmCommand(Accumulator acc, ExecutionContext ctx) {
        String executable = "${nodeModules}/@biomejs/biome/bin/biome";
        String command = executable + " lint ${repoDir} --fix";
        return Arrays.asList("/bin/bash", "-c", command);
    }

}
