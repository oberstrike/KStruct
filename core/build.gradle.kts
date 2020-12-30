plugins {
    kotlin("jvm")
    kotlin("kapt")
}

dependencies {
    implementation("org.mapstruct:mapstruct:1.4.1.Final")
    implementation(kotlin("stdlib"))
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.google.auto.service:auto-service:1.0-rc4")
    kapt("com.google.auto.service:auto-service:1.0-rc4")
    implementation("com.squareup:kotlinpoet:1.6.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.3.72")

    testImplementation("io.mockk:mockk:1.10.2")
    testImplementation("org.jooq:joor:0.9.13")
    implementation("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.2.0")
    implementation("com.squareup:kotlinpoet:1.7.2")
    implementation("com.squareup:kotlinpoet-metadata:1.7.2")
    implementation("com.squareup:kotlinpoet-metadata-specs:1.7.2")
    implementation("com.squareup:kotlinpoet-classinspector-elements:1.7.2")
    implementation("com.squareup:kotlinpoet-classinspector-reflective:1.7.2")
    implementation("com.github.tschuchortdev:kotlin-compile-testing:1.3.1")

}
