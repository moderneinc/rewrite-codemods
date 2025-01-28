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
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.test.SourceSpecs.text;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
class Ui5Test implements RewriteTest {

    @DocumentExample
    @Test
    @Disabled("Work in progress")
    void noRules() {
        rewriteRun(
          spec -> spec.recipe(new Ui5()).typeValidationOptions(TypeValidation.all().immutableExecutionContext(false)),
          text(
            //language=xml
            """
              <mvc:View
              	controllerName="sap.ui.demo.cart.controller.NotFound"
              	xmlns:mvc="sap.ui.core.mvc"
              	xmlns:core="sap.ui.core"
              	xmlns="sap.m">
              	<Page
              		id="page"
              		title="{i18n>categoryNoData}">
              		<landmarkInfo>
              			<PageAccessibleLandmarkInfo
              				rootRole="Region"
              				rootLabel="{i18n>NotFound_rootLabel}"
              				contentRole="Main"
              				contentLabel="{i18n>NotFound_contentLabel}"
              				headerRole="Region"
              				headerLabel="{i18n>NotFound_headerLabel}"/>
              		</landmarkInfo>
              		<content>
              			<MessagePage
              				showHeader="false"
              				text="{i18n>categoryNoData}"
              				description=""/>
              		</content>
              	</Page>
              </mvc:View>
              """,
            //language=xml
            """
              <mvc:View
              	controllerName="sap.ui.demo.cart.controller.NotFound"
              	xmlns:mvc="sap.ui.core.mvc"
              	xmlns:core="sap.ui.core"
              	xmlns="sap.m">
              	<Page
              		id="page"
              		title="{i18n>categoryNoData}">
              		<landmarkInfo>
              			<PageAccessibleLandmarkInfo
              				rootRole="Region"
              				rootLabel="{i18n>NotFound_rootLabel}"
              				contentRole="Main"
              				contentLabel="{i18n>NotFound_contentLabel}"
              				headerRole="Region"
              				headerLabel="{i18n>NotFound_headerLabel}"/>
              		</landmarkInfo>
              		<content>
              			<MessagePage
              				showHeader="false"
              				text="{i18n>categoryNoData}"
              				description=""/>
              		</content>
              	</Page>
              </mvc:View>
              """,
            spec -> spec.path("view.xml")
          )
        );
    }
}
