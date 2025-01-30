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

class NextJsCodemodsTest implements RewriteTest {

    @SuppressWarnings({"NpmUsedModulesInstalled", "JSUnusedLocalSymbols"})
    @Test
    @DocumentExample
    void builtInNextFont() {
        rewriteRun(
          spec -> spec.recipeFromResource("/META-INF/rewrite/nextjs.yml", "org.openrewrite.codemods.migrate.nextjs.v13_2.BuiltInNextFont").typeValidationOptions(TypeValidation.all().immutableExecutionContext(false)),
          text(
            //language=js
            """
              import { Fira_Code } from "@next/font/google"
              
              const firaCode = Fira_Code({
                  weight: "500",
                  subsets: ["latin"]
              })
              """,
            //language=js
            """
              import { Fira_Code } from "next/font/google"
              
              const firaCode = Fira_Code({
                  weight: "500",
                  subsets: ["latin"]
              })
              """,
            spec -> spec.path("src/components/Code.js")
          )
        );
    }
}
