plugins {
    application
    kotlin("jvm")
    kotlin("kapt")
}

application {
    mainClassName = "cli.Main"
}

dependencies {
    implementation(project(":core"))
    kapt(project(":core"))
    implementation(project(":codegen"))
    kapt(project(":codegen"))
    implementation(kotlin("stdlib"))
    implementation("org.mapstruct:mapstruct:1.4.1.Final")
    kapt("org.mapstruct:mapstruct-processor:1.4.1.Final")



}
