/*
 * Moderne Proprietary. Only for use by Moderne customers under the terms of a commercial contract.
 */
package org.openrewrite.codemods;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.test.SourceSpecs.text;

class NextJsCodemodsTest implements RewriteTest {

    @SuppressWarnings({"NpmUsedModulesInstalled", "JSUnusedLocalSymbols"})
    @Test
    @DisabledIfEnvironmentVariable(named = "CI", matches = "true")
    void builtInNextFont() {
        rewriteRun(
          spec -> spec.recipeFromResource("/META-INF/rewrite/nextjs.yml", "org.openrewrite.codemods.nextjs.v13_2.BuiltInNextFont"),
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
