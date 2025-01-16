/*
 * Copyright 2025 the original author or authors.
 *
 * Moderne Proprietary. Only for use by Moderne customers under the terms of a commercial contract.
 */
package org.openrewrite.codemods;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.test.SourceSpecs.text;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
class ApplyCodemodTest implements RewriteTest {

    @DocumentExample
    @Test
    void formatStatement() {
        rewriteRun(
          spec -> spec.recipe(new ApplyCodemod("@kevinbarabash/codemods/transforms/array.js", null, null, null)),
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
          spec -> spec.recipe(new ApplyCodemod( "react-declassify", "@codemod/cli/bin/codemod --plugin", "**/*.(j|t)sx", null)),
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
