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
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.test.SourceSpecs.text;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
class ApplyCodemodTest implements RewriteTest {

    @DocumentExample
    @Test
    void formatStatement() {
        rewriteRun(
          spec -> spec.recipe(new ApplyCodemod("@kevinbarabash/codemods/transforms/array.js", null, null, null)).typeValidationOptions(TypeValidation.all().immutableExecutionContext(false)),
          text(
            //language=js
            """
              _.tail(x)
              """,
            """
              x.slice(1)
              """,
            spec -> spec.path("src/Foo.js")
          )
        );
    }

    @Test
    void formatReactStatement() {
        rewriteRun(
          spec -> spec.recipe(new ApplyCodemod("react-declassify", "@codemod/cli/bin/codemod --plugin", "**/*.(j|t)sx", null)).typeValidationOptions(TypeValidation.all().immutableExecutionContext(false)),
          text(
            //language=js
            """
              import React from "react";
              
                        export class C extends React.Component {
                          render() {
                            const { text, color } = this.props;
                            return <button style={{ color }} onClick={() => this.onClick()}>{text}</button>;
                          }
              
                          onClick() {
                            const { text, handleClick } = this.props;
                            alert(`${text} was clicked!`);
                            handleClick();
                          }
                        }
              """,
            """
              import React from "react";
              
                        export const C = props => {
                          const {
                            text,
                            color,
                            handleClick
                          } = props;
              
                          function onClick() {
                            alert(`${text} was clicked!`);
                            handleClick();
                          }
              
                          return <button style={{ color }} onClick={() => onClick()}>{text}</button>;
                        };
              """,
            spec -> spec.path("src/Foo.js")
          )
        );
    }
}
