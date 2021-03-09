KStruct - The Kotlin-Annotation-Processor for extending Mapstruct and other Frameworks
====================

A The project has 2 modules:

 1. [codegen](./codegen) implements the main
 2. [cli](./cli) implements the command line interface

Common behavior for all projects, such as the configuration of _group_, _version_ and _repositories_, is defined in the [root project build script](./build.gradle.kts).

Plugin application and dependency configuration is segregated to each separate subproject.

Run with:

    ./gradlew run

Check all project dependencies with:

    ./gradlew dependencies
