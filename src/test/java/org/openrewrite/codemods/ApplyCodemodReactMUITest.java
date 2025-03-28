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
