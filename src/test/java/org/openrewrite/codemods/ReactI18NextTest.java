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
class ReactI18NextTest implements RewriteTest {

    @DocumentExample
    @Test
    void internationalizeReactComponent() {
        rewriteRun(
          spec -> spec.recipe(new ReactI18Next(null, "react-i18next", null, null, null))
                  .typeValidationOptions(TypeValidation.all().immutableExecutionContext(false))
                  .expectedCyclesThatMakeChanges(2),
          text(
            //language=jsx
            """
              import React from 'react';

              function Welcome() {
                return (
                  <div>
                    <h1>Welcome to Our Platform</h1>
                    <img src="profile.jpg" alt="User profile picture" />
                    <p>Hello World</p>
                  </div>
                );
              }

              export default Welcome;
              """,
            //language=jsx
            """
              import React from 'react';
              import { useTranslation } from 'react-i18next';

              function Welcome() {
                const { t } = useTranslation();
                return (
                  <div>
                    <h1>{t('welcome-to-our-platform')}</h1>
                    <img src="profile.jpg" alt={t('user-profile-picture')} />
                    <p>{t('hello-world')}</p>
                  </div>
                );
              }

              export default Welcome;
              """,
            spec -> spec.path("src/Welcome.jsx")
          ),
          text(
            """
              {}
              """,
            """
              {
                "welcome-to-our-platform": "Welcome to Our Platform",
                "user-profile-picture": "User profile picture",
                "hello-world": "Hello World"
              }
              """,
            spec -> spec.path("translations.json")
          )
        );
    }

    @Test
    void internationalizeWithTranslationRoot() {
        rewriteRun(
          spec -> spec.recipe(new ReactI18Next(null, "react-i18next", "common", null, null))
                  .typeValidationOptions(TypeValidation.all().immutableExecutionContext(false))
                  .expectedCyclesThatMakeChanges(2),
          text(
            //language=jsx
            """
              import React from 'react';

              function Button() {
                return <button>Click me</button>;
              }

              export default Button;
              """,
            //language=jsx
            """
              import React from 'react';
              import { useTranslation } from 'react-i18next';

              function Button() {
                const { t } = useTranslation();
                return <button>{t('common.click-me')}</button>;
              }

              export default Button;
              """,
            spec -> spec.path("src/Button.jsx")
          ),
          text(
            """
              {}
              """,
            """
              {
                "common": {
                  "click-me": "Click me"
                }
              }
              """,
            spec -> spec.path("translations.json")
          )
        );
    }

    @Test
    void internationalizeWithCustomTranslationPath() {
        rewriteRun(
          spec -> spec.recipe(new ReactI18Next("./src/locales/en.json", "react-i18next", null, null, null))
                  .typeValidationOptions(TypeValidation.all().immutableExecutionContext(false))
                  .expectedCyclesThatMakeChanges(2),
          text(
            //language=jsx
            """
              import React from 'react';

              function Header() {
                return <h1>Page Title</h1>;
              }

              export default Header;
              """,
            //language=jsx
            """
              import React from 'react';
              import { useTranslation } from 'react-i18next';

              function Header() {
                const { t } = useTranslation();
                return <h1>{t('page-title')}</h1>;
              }

              export default Header;
              """,
            spec -> spec.path("src/Header.jsx")
          ),
          text(
            """
              {}
              """,
            """
              {
                "page-title": "Page Title"
              }
              """,
            spec -> spec.path("src/locales/en.json")
          )
        );
    }
}
