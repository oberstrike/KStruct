plugins {
    application
    kotlin("jvm")
    kotlin("kapt")
}

application {
    mainClassName = "cli.Main"
}

dependencies {
    implementation(project(":codegen"))
    kapt(project(":codegen"))
    implementation(kotlin("stdlib"))
    implementation("org.mapstruct:mapstruct:1.4.1.Final")
    kapt("org.mapstruct:mapstruct-processor:1.4.1.Final")
    implementation("javax.enterprise:cdi-api:2.0.SP1")
    implementation("io.quarkus:quarkus-hibernate-orm-panache-kotlin-deployment:1.10.5.Final")



}
