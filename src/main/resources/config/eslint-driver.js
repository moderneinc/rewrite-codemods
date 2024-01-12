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
const optionator = require('optionator')({
    prepend: 'Usage: eslint-driver [options]',
    options: [{
        option: 'help',
        alias: 'h',
        type: 'Boolean',
        description: 'displays help',
    }, {
        option: 'format',
        type: 'Boolean',
        description: 'Format source code using Prettier.',
    }, {
        option: 'patterns',
        type: '[String]',
        concatRepeatedArrays: true,
        description: 'The lint target files. This can contain any of file paths, directory paths, and glob patterns.',
    }, {
        option: 'env',
        type: 'Object',
        concatRepeatedArrays: true,
        description: 'Env setting for ESLint.',
    }]
});

(async function main() {
    const options = optionator.parseArgv(process.argv);
    if (options.help) {
        console.log(optionator.generateHelp());
        return;
    }

    const patterns = options['patterns'] || ['**/*.js', '**/*.jsx'];
    const env = options['env'] || {};
    const format = options['format'];

    const plugins = ["@typescript-eslint"];
    const extend = ["eslint:recommended", "plugin:@typescript-eslint/recommended"];
    const rules = {
        "eqeqeq": "error",
        "no-duplicate-imports": "error",
    };
    if (format) {
        plugins.push("prettier");
        extend.push("plugin:prettier/recommended");
        rules["prettier/prettier"] = [ "error", {}, { "usePrettierrc": false } ]
    }

    const eslint = new ESLint(
        {
            cwd: process.cwd(),
            errorOnUnmatchedPattern: false,
            allowInlineConfig: false,
            overrideConfig: {
                parser: "@typescript-eslint/parser",
                parserOptions: {
                    ecmaVersion: "latest",
                    ecmaFeatures: {
                        jsx: true
                    },
                    sourceType: "module"
                },
                env: env,
                plugins: plugins,
                extends: extend,
                rules: rules
            },
            // overrideConfigFile: "config/prettier.eslintrc.json",
            // resolvePluginsRelativeTo: "../codemods-npm",
            useEslintrc: false,
            fix: true,
            fixTypes: ["directive", "problem", "suggestion", "layout"]
        }
    );

    const results = await eslint.lintFiles(patterns);
    const formatter = await eslint.loadFormatter("json-with-metadata");
    const resultText = formatter.format(results);

    console.log(resultText);
    await ESLint.outputFixes(results);
})().catch((error) => {
    process.exitCode = 1;
    console.error(error);
})
