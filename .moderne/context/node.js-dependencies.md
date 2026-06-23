# Node.js Dependencies

## npm package dependencies with versions, scopes, and licenses

Node.js package dependencies from package.json including resolved versions, dependency scopes (dependencies, devDependencies, etc.), direct vs transitive, and license information. Use this to understand what npm packages the project uses and avoid suggesting conflicting dependencies.

## Data Tables

### Node.js dependencies in use

**File:** [`node-dependencies-in-use.csv`](node-dependencies-in-use.csv)

Direct and transitive dependencies in use in Node.js projects.

| Column | Description |
|--------|-------------|
| Project name | The name of the project that contains the dependency (from package.json). |
| Project path | The path to the project. |
| Package name | The name of the npm package. |
| Version | The resolved version of the package. |
| Version constraint | The version constraint as declared in package.json. |
| Scope | Dependency scope: dependencies, devDependencies, peerDependencies, optionalDependencies, or bundledDependencies. |
| Direct | Whether this is a direct dependency (true) or transitive dependency (false). |
| Count | How many times this dependency appears in the dependency tree. |
| License | The SPDX license identifier of the package, if available. |

