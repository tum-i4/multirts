# MultiRTS: Build System Aware Multi-language Regression Test Selection

MultiRTS is a research tool for regression test selection.
It therefore uses test-wise coverage from [JTeC](https://github.com/tum-i4/JTeC).

## Structure

```
├── multirts-core             <- The MultiRTS core package contains code for test and module selection.
└── multirts-maven-plugin     <- MultiRTS Maven plugin for steering the test and module selection.
```

## Setup

To build MultiRTS simply run:

```shell
$ mvn clean install 
```

This will build the code for all MultiRTS projects, run all tests, and install the JARs to your local Maven repository.
Note: In order to successfully build MultiRTS, you'll need to have the appropriate [JTeC](https://github.com/tum-i4/JTeC) version in your local Maven
repository.

## Usage

MultiRTS is built to be used with Maven Surefire and Failsafe.
Therefore, the simplest way to use MultiRTS in a Maven project is through the Maven plugin:

```xml

<build>
    <plugins>
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
$ mvn test -fn -Dsurefire.includesFile="includes.txt"
```

## Acknowledgments & Contributors

MultiRTS is mainly developed by Daniel Elsner, whose research has been supported
by IVU Traffic Technologies.
As part of the BMBF-funded SOFIE and Q-Soft projects, Raphael Noemmer (CQSE) has further contributed significantly to
JTeC and other projects that lead to MultiRTS.