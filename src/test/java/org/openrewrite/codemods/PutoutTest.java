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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import java.util.Set;

import static org.openrewrite.test.SourceSpecs.text;

class PutoutTest implements RewriteTest {

    @Disabled("Started to fail; discussed on Slack and disabled for now")
    @Test
    void noRules() {
        rewriteRun(
          spec -> spec.recipe(new Putout(null, null)).typeValidationOptions(TypeValidation.all().immutableExecutionContext(false)),
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

    @DocumentExample
    @Test
    void withRules() {
        rewriteRun(
          spec -> spec.recipe(new Putout(Set.of("conditions/merge-if-statements"), null)).typeValidationOptions(TypeValidation.all().immutableExecutionContext(false)),
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
          spec -> spec.recipe(new Putout(null, null)).typeValidationOptions(TypeValidation.all().immutableExecutionContext(false)),
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
    @Disabled("runs fine locally but intermittently fails on CI")
    void printer() {
        rewriteRun(
          spec -> spec.recipe(new Putout(null, "recast")).typeValidationOptions(TypeValidation.all().immutableExecutionContext(false)),
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
