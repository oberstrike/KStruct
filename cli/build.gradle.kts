plugins {
    application
    kotlin("jvm")
    kotlin("kapt")
}

application {
    mainClassName = "cli.Main"
}

dependencies {
    implementation(kotlin("stdlib"))
}
