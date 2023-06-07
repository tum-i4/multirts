# MultiRTS Maven Extension

A Maven extension that can be used to filter the Maven modules in the Maven lifecycle of a reactor.
Use this to selectively compile (and test) certain Maven modules only.
Maven does come with `--project`, `--also-make`, and `--also-make-dependents` options, but those don't offer a way to
provide Maven module names (or paths) in files and therefore are limited in environments with command line length
limit (e.g. CMD.exe on Windows).
Also, these options don't support *transitively* finding all modules that need to be built for a set of changed modules,
but are limited to building all up-/downstream modules (and *not* all transitive upstream modules for all downstream
modules).

| Key                          | Type             | Description                                                                                                                                 |
|------------------------------|------------------|---------------------------------------------------------------------------------------------------------------------------------------------|
| `multirts.transitiveModules` | `List[Path]`     | Comma-separated list of filepaths that include new-line separated Maven module paths; will execute all transitively dependent Maven modules |
| `multirts.upstreamModules`   | `List[Path]`     | Comma-separated list of filepaths that include new-line separated Maven module paths; will execute all transitive upstream Maven modules    |
| `multirts.downstreamModules` | `List[Path]`     | Comma-separated list of filepaths that include new-line separated Maven module paths; will execute all transitive downstream Maven modules  |
| `multirts.moduleOutput`      | `Optional[Path]` | Filepath where to write selected Maven modules (optional)                                                                                   |
| `multirts.filterExecution`   | `Boolean`        | Filter the Maven execution to the selected Maven modules; default: `false` (will only compute the set of modules, but not filter them)      |
