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
            //language=HTML
            """
              <!DOCTYPE html>
              <html>
              	<head>
              		<title>Testing Overview</title>
              		<!--  try to load the basic UI5 styles -->
              		<link rel="stylesheet" type="text/css" href="resources/sap/ui/core/themes/sap_belize/library.css">
              		</head>
              		<body class="sapUiBody sapUiMediumMargin sapUiForceWidthAuto">
              			<h1>Testing Overview</h1>
              			<p>This is an overview page of various ways to test the generated app during development.<br/>
              Choose one of the access points below to launch the app as a standalone application, e.g. on a Tomcat server.</p>
              
              			<ul>
              				<li>
              					<a href="index.html">index.html</a> - start the app standalone (using OpenUI5 and local grunt tooling)</li>
              				<li>
              					<a href="index_cdn.html">index_cdn.html</a> - start the app standalone (using OpenUI5 CDN)</li>
              				<li>
              					<a href="test/flpSandbox.html">test/flpSandbox.html</a> - start the app in Fiori Launchpad sandbox (using SAPUI5 CDN)</li>
              			</ul>
              		</body>
              	</html>
              """,
            //language=HTML
            """
              <!DOCTYPE html>
              <html>
              	<head>
              		<title>Testing Overview</title>
              		<!--  try to load the basic UI5 styles -->
              		<link rel="stylesheet" type="text/css" href=~~(Use of deprecated theme 'sap_belize')~~>"resources/sap/ui/core/themes/sap_belize/library.css">
              		</head>
              		<body class="sapUiBody sapUiMediumMargin sapUiForceWidthAuto">
              			<h1>Testing Overview</h1>
              			<p>This is an overview page of various ways to test the generated app during development.<br/>
              Choose one of the access points below to launch the app as a standalone application, e.g. on a Tomcat server.</p>
              
              			<ul>
              				<li>
              					<a href="index.html">index.html</a> - start the app standalone (using OpenUI5 and local grunt tooling)</li>
              				<li>
              					<a href="index_cdn.html">index_cdn.html</a> - start the app standalone (using OpenUI5 CDN)</li>
              				<li>
              					<a href="test/flpSandbox.html">test/flpSandbox.html</a> - start the app in Fiori Launchpad sandbox (using SAPUI5 CDN)</li>
              			</ul>
              		</body>
              	</html>
              """,
            spec -> spec.path("webapp/test.html")
          ),
          text(
            //language=YAML
            """
              specVersion: '0.1'
              metadata:
                name: test-app
              type: application
              """,
            //language=YAML
            """
              specVersion: '0.1'
              metadata:
                name: test-app
              type: application
              """,
            spec -> spec.path("ui5.yaml")
          ),
          text(
            //language=JSON
            """
              {
                "sap.app": {
                  "id": "my.ui5.app",
                  "type": "application",
                  "applicationVersion": {
                    "version": "1.0.0"
                  }
                }
              }
              """,
            //language=JSON
            """
              {
                "sap.app": {
                  "id": "my.ui5.app",
                  "type": "application",
                  "applicationVersion": {
                    "version": "1.0.0"
                  }
                }
              }
              """,
            // TODO: test js file
//          text(
//            //language=JS
//            """
//              sap.ui.define(
//                [
//                  './BaseController',
//                  'sap/ui/model/json/JSONModel',
//                  '../model/formatter',
//                  'sap/base/util/UriParameters',
//                ],
//                function (BaseController, JSONModel, formatter, UriParameters) {
//                  'use strict';
//                }
//              );
//              """,
//            //language=JS
//            """
//              sap.ui.define(
//                [
//                  './BaseController',
//                  'sap/ui/model/json/JSONModel',
//                  '../model/formatter',
//                  ~~(Import of deprecated module 'sap/base/util/UriParameters')~~>'sap/base/util/UriParameters',
//                ],
//                function (BaseController, JSONModel, formatter, UriParameters) {
//                  'use strict';
//                }
//              );
//              """,
//            spec -> spec.path("webapp/test.js")
//          ),
            spec -> spec.path("webapp/manifest.json")
          )
        );
    }
}
