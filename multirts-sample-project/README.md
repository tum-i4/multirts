# MultiRTS Sample Project

This project demonstrates how to use MultiRTS together with JTeC.

## Collect Test Traces with JTeC

To obtain the test traces necessary for selecting tests, we use JTeC.
Note that we collect coverage at class level and collect all opened files.

```shell
$ mvn clean verify -Pjtec -Djtec.opts="test.trace,cov.trace,cov.instr,test.instr=false,sys.trace,sys.file,sys.socket=false,sys.thread=false,sys.process=false,test.reuseForks"
$ mvn -Pjtec jtec:report-aggregate
```

## Introduce Changes

```shell
$ git apply sample-diff.patch
```

