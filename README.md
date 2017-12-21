# SmartASMifier
A utility to compile Java source code to ASM source code(or bytecode as text)

## Prerequisite
* Install JDK 8+
* Install Apache Groovy 2.4.12+

## Usage
```
./asmify.sh [-b] <the paths of java source files>
```

## For example  
**Show ASM source code**:  
```
./asmify.sh jsrc/HelloWorld.java jsrc/HelloWorld2.java
```

**Show bytecode**:  
```
./asmify.sh -b jsrc/HelloWorld.java jsrc/HelloWorld2.java
```
