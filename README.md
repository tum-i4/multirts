# MultiRTS: Build System Aware Multi-language Regression Test Selection

MultiRTS is a research tool for regression test selection.
It therefore uses test traces generated from [JTeC](https://github.com/tum-i4/JTeC).

*Note: Although very similar, this is not the original implementation from the [ICSE-SEIP'22 paper](https://doi.org/10.1145/3510457.3513078), which relied on DTrace for tracing system calls to files and 
had a Python CLI to steer the test/module selection process.*

## Structure

```
├── multirts-core             <- The MultiRTS core package contains code for test and module selection.
├── multirts-maven-extension  <- MultiRTS Maven extension for selective Maven reactor build.
└── multirts-maven-plugin     <- MultiRTS Maven plugin for steering the test and module selection.
```

## Setup

To build MultiRTS simply run:

```shell
$ mvn clean install 
```

This will build the code for all MultiRTS projects, run all tests, and install the JARs to your local Maven repository.

*Note: In order to successfully build MultiRTS, you'll need to have the appropriate [JTeC](https://github.com/tum-i4/JTeC) version in your local Maven
repository.*

## Usage

MultiRTS is built to be used with Maven Surefire and Failsafe.
Therefore, the simplest way to use MultiRTS in a Maven project is through the Maven extension and plugin:

```xml

<build>
    <plugins>
        <!-- The extension hooks into Maven lifecycle to build only selected modules. -->
        <plugin>
            <groupId>edu.tum.sse</groupId>
            <artifactId>multirts-maven-extension</artifactId>
            <version>0.0.1-SNAPSHOT</version>
            <extension>true</extension>
        </plugin>
        <!-- The plugin provides goals for module and test selection. -->
        <plugin>
            <groupId>edu.tum.sse</groupId>
            <artifactId>multirts-maven-plugin</artifactId>
            <version>0.0.1-SNAPSHOT</version>
        </plugin>
    </plugins>
</build>
```

MultiRTS generates a `includes.txt` and `excludes.txt` file which can then be passed to Maven Surefire or Failsafe:

```shell
$ mvn test -fn -Dsurefire.excludesFile="excludes.txt"
```

## Acknowledgments & Contributors

MultiRTS has been developed by Daniel Elsner, whose research has been supported by IVU Traffic Technologies.
As part of the BMBF-funded SOFIE and Q-Soft projects, Raphael Noemmer (CQSE) has further contributed significantly to
JTeC and other projects that lead to MultiRTS.