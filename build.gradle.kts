plugins {
    kotlin("jvm") version "1.4.10" apply false
    kotlin("kapt") version "1.4.10" apply false
}

allprojects {

    group = "org.gradle.kotlin.dsl.samples.multiproject"

    version = "1.0"

    repositories {
        jcenter()
        maven(url="https://dl.bintray.com/oberstrike/maven")
    }
}

