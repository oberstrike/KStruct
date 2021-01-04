import java.util.*

plugins {
    application
    kotlin("jvm")
    kotlin("kapt")
    `java-library`
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.5"
    id("org.jetbrains.dokka") version "1.4.0"
}

dependencies {
    implementation(project(":core"))
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
    implementation("javax.enterprise:cdi-api:2.0.SP1")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

val myGroupId = "com.maju.proxy"
val myArtifactId = "proxy-generator"
val myVersion = "1.0.0"

val dokkaJavadocJar by tasks.creating(Jar::class) {
    dependsOn(tasks.dokkaJavadoc)
    from(tasks.dokkaJavadoc.get().outputDirectory.get())
    archiveClassifier.set("javadoc")
}

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.getByName("main").allSource)
    from("LICENCE.md") {
        into("META-INF")
    }
}

val pomUrl = "https://github.com/oberstrike/KStruct"
val pomScmUrl = "https://github.com/oberstrike/KStruct"
val pomIssueUrl = "https://github.com/oberstrike/KStruct/issues"
val pomDesc = "https://github.com/oberstrike/KStruct"

val pomLicenseName = "MIT"
val pomLicenseUrl = "https://opensource.org/licenses/mit-license.php"

val pomDeveloperId = "oberstrike"
val pomDeveloperName = "Markus Jürgens"


publishing {
    publications {
        create<MavenPublication>("kstruct-proxy") {
            groupId = myGroupId
            artifactId = myArtifactId
            version = myVersion
            from(components["java"])
            artifact(sourcesJar)
            artifact(dokkaJavadocJar)

            pom {
                packaging = "jar"
                name.set(project.name)
                description.set("A library to generate boilerplate code")
                url.set(pomUrl)
                scm {
                    url.set(pomScmUrl)
                }
                issueManagement {
                    url.set(pomIssueUrl)
                }
                licenses {
                    license {
                        name.set(pomLicenseName)
                        url.set(pomLicenseUrl)
                    }
                }
                developers {
                    developer {
                        id.set(pomDeveloperId)
                        name.set(pomDeveloperName)
                    }
                }
            }
        }
    }
}

bintray {
    user = project.findProperty("bintrayUser").toString()
    key = project.findProperty("bintrayKey").toString()
    publish = !project.version.toString().endsWith("SNAPSHOT")

    setPublications("kstruct-proxy")

    pkg.apply {
        repo = "maven"
        name = myArtifactId
        userOrg = "oberstrike"
        githubRepo = githubRepo
        vcsUrl = pomScmUrl
        description = "A framework to generate openapi code."
        setLabels("kotlin", "kotlinpoet", "proxy")
        setLicenses("MIT")
        desc = description
        websiteUrl = pomUrl
        issueTrackerUrl = pomIssueUrl
        githubReleaseNotesFile = "README.md"

        version.apply {
            //      name = myArtifactId
            desc = pomDesc
            released = Date().toString()
            vcsTag = "v$myVersion"
        }
    }


}


