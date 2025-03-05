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
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.test.SourceSpecs.text;

public class BiomeTest implements RewriteTest {

    @DocumentExample
    @Test
    void noRules() {
        rewriteRun(
                spec -> spec.recipe(new Biome()).typeValidationOptions(TypeValidation.all().immutableExecutionContext(false)),
                text(
                        //language=js
                        """
                                <a href="http://external.link" target="_blank" rel="noopener">child</a>
                                """,
                        """
                                <a href="http://external.link" target="_blank" rel="noreferrer noopener">child</a>
                                """,
                        spec -> spec.path("src/Foo.jsx")
                )
        );
    }
}
