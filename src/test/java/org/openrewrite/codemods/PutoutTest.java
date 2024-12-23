/*
 * Moderne Proprietary. Only for use by Moderne customers under the terms of a commercial contract.
 */
package org.openrewrite.codemods;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RewriteTest;

import java.util.Set;

import static org.openrewrite.test.SourceSpecs.text;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
class PutoutTest implements RewriteTest {

    @DocumentExample
    @Test
    void noRules() {
        rewriteRun(
          spec -> spec.recipe(new Putout(null, null)),
          text(
            //language=js
            """
              function notUsed() {}
              export function used() {}
              """,
            """
              export function used() {}
              
              """,
            spec -> spec.path("src/Foo.js")
          )
        );
    }

    @Test
    void withRules() {
        rewriteRun(
          spec -> spec.recipe(new Putout(Set.of("conditions/merge-if-statements"), null)),
          text(
            //language=js
            """
              function notUsed() {}
              export function used() {}
              
              if (a > b)
                  if (b < c)
                      console.log('hi');
              """,
            """
              function notUsed() {}
              
              export function used() {}
              if (a > b && b < c)
                  console.log('hi');
              
              """,
            spec -> spec.path("src/Foo.js")
          )
        );
    }

    @Test
    void jsx() {
        rewriteRun(
          spec -> spec.recipe(new Putout(null, null)),
          text(
            //language=js
            """
              export function Test() {
                  const greet = 'Hello World';
                  const unused = 'unused';
                  return (
                      <div>
                          <h1>{greet}</h1>
                      </div>
                  );
              }
              """,
            """
              export function Test() {
                  const greet = 'Hello World';
                 \s
                  return (
                      <div>
                          <h1>{greet}</h1>
                      </div>
                  );
              }
              
              """,
            spec -> spec.path("src/Foo.jsx")
          )
        );
    }

    /**
     * recast should keep format close to original while putout would format it
     * have all props on a new line, leave trailing commas, etc.
     */
    @Test
    void printer() {
        rewriteRun(
          spec -> spec.recipe(new Putout(null, "recast")),
          text(
            //language=js
            """
              export const square = (x) => {
                return x * x
              }
              export const sum = (a, b) => { return a + b };
              export const obj = {
                key: 'value',
                method: function() {
                      return something;
                },
                sum, square
              };
              """,
            """
              export const square = x => x ** 2
              export const sum = (a, b) => a + b;
              export const obj = {
                key: 'value',
                method: function() {
                      return something;
                },
                sum, square
              };
              """,
            spec -> spec.path("src/Foo.js")
          )
        );
    }
}
