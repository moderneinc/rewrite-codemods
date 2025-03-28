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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.openrewrite.config.Environment;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.test.SourceSpecs.text;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
public class ApplyCodemodReactMUITest implements RewriteTest {
    @Test
    void autocompleteRenameCloseicon() {
        rewriteRun(
            spec -> spec.recipe(Environment.builder()
                            .scanRuntimeClasspath()
                            .build()
                            .activateRecipes("org.openrewrite.codemods.migrate.mui.AutocompleteRenameCloseicon"))
                    .typeValidationOptions(TypeValidation.all().immutableExecutionContext(false)),
            text(
                """
                <Autocomplete
                  closeIcon='toggleInput'
                />
                """,
                """
                <Autocomplete
                  clearIcon='toggleInput'
                />
                """,
                spec -> spec.path("src/foo.js")
            )
        );
    }
}
