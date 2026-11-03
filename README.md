# Java Xfoil Wrapper

This is a simple Java wrapper for [Xfoil](https://web.mit.edu/drela/Public/web/xfoil/).

The current goal of this library is to retrieve the `Cl` and `Cd` values given a NACA profile, angle of attack, Mach number, and Reynolds number.

The underlying process interface also allows for more detailed scripting of command input and validating output.

## Requirements

- JDK 21 or newer for building.
- Gradle (or use the included Gradle Wrapper).
- Java 21 or newer for running, provided all runtime dependencies support Java 21 (This is for building for matlab).
- Xfoil installed and available on your system.

## Building

This project uses Gradle for compilation and dependency management.

Build the JAR:

```sh
./gradlew clean jar
```

The generated JAR will be located at:

```text
build/libs/xfoil-1.0.0.jar
```

Build the entire project:

```sh
./gradlew build
```

## Examples

See the `examples/` directory for usage examples.

For matlab jar file is needed to be loaded

`xFoil.exe` also need to be run inside pseudoterminal otherwise it does not output to pipes thus I tested with `ssh -tt` but other tools can be used.

### Running Examples
```sh
./gradlew runExample
```
