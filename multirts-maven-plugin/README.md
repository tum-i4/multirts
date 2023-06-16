# MultiRTS Maven Plugin

A Maven plugin to select affected modules and tests for selective building and testing.

## Goals

Two goals are available:

```shell
$ mvn multirts:module-selection  # use it to select changed modules (1)
$ mvn multirts:test-selection  # use it to select tests and their respective modules (2)
```

Both goals (1) and (2) create a `modules.txt` file which contains the modules that are either (1) affected through the
introduced
changes themselves or (2) contain tests that have been selected.
Additionally, the test selection (2) outputs an `included.txt` file, containing all the selected tests.
The `included.txt` file can be passed directly to Maven Surefire/Failsafe using
the `-Dsurefire.includesFile` option.

## Parameters

| Key                       | Type      | Description                                                                                        |
|---------------------------|-----------|----------------------------------------------------------------------------------------------------|
| `multirts.debug`          | `Boolean` | Enables more verbose debug output                                                                  |
| `multirts.git`            | `Path`    | Path to git repository root (default: Maven root project directory)                                |
| `multirts.fileFilter`     | `String`  | Regex to filter files in changeset                                                                 |
| `multirts.label`          | `String`  | Label which is used for naming generated file artifacts                                            |
| `multirts.output`         | `Path`    | Output directory where to store generated file artifacts (default: target/.multirts)               |
| `multirts.sourceRevision` | `String`  | The source revision (commit identifier or branch name) where the changes are currently versioned   |
| `multirts.targetRevision` | `String`  | The target revision (commit identifier or branch name) into which the changes are to be integrated |

## Additional Parameters for Module Selection Goal

| Key                  | Type     | Description                                                                                            |
|----------------------|----------|--------------------------------------------------------------------------------------------------------|
| `multirts.fullBuild` | `String` | Comma-separated string of file paths that should trigger a full build                                  |
| `multirts.stripDirs` | `String` | Comma-separated string of directories to strip away and use their parent modules (default: p2,feature) |

## Additional Parameters for Test Selection Goal

| Key                               | Type         | Description                                                                                  |
|-----------------------------------|--------------|----------------------------------------------------------------------------------------------|
| `multirts.additionalFileMappings` | `List[Path]` | CSV files delimited by `;` containing two columns (used for DLL-to-source-file mapping)      |
| `multirts.includedTests`          | `String`     | Comma-separated string of additionally included tests (default: `**/PackageDependencyTest*`) |
| `multirts.testReport`             | `Path`       | JTeC test report                                                                             |
