/*
 * Copyright 2024 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
const {ESLint} = require("eslint");

(async function main() {
    const eslint = new ESLint(
        {
            cwd: process.cwd(),
            errorOnUnmatchedPattern: false,
            allowInlineConfig: false,
            overrideConfig: {
                parserOptions: {
                    ecmaVersion: "latest",
                    ecmaFeatures: {
                        jsx: true
                    },
                    sourceType: "module"
                },
                plugins: ["prettier"],
                rules: {
                    "prettier/prettier": [
                        "error",
                        {},
                        {
                            "usePrettierrc": false
                        }
                    ]
                }
            },
            // overrideConfigFile: "config/prettier.eslintrc.json",
            // resolvePluginsRelativeTo: "../codemods-npm",
            useEslintrc: false,
            fix: true,
            fixTypes: ["directive", "problem", "suggestion", "layout"]
        }
    );

    const results = await eslint.lintFiles(["**/*.js", "**/*.jsx"]);
    // const formatter = await eslint.loadFormatter("stylish");
    // const resultText = formatter.format(results);

    // 4. Output it.
    // console.log(resultText);
    await ESLint.outputFixes(results);
})().catch((error) => {
    process.exitCode = 1;
    console.error(error);
})