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
    val kotlinpoetVersion = "1.9.0"
    val kotlinVersion = "1.5.10"
    val quarkusVersion = "1.13.7.Final"

    implementation(kotlin("stdlib"))

    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("com.google.auto.service:auto-service:1.0")
    kapt("com.google.auto.service:auto-service:1.0")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    //testImplementation("org.jetbrains.kotlin:kotlin-test-junit")

    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

    //Panache for quarkus
    implementation("io.quarkus:quarkus-hibernate-orm-panache-kotlin-deployment:$quarkusVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.3.0")
    implementation("org.springframework.data:spring-data-jpa:2.5.2")
    
    implementation("com.squareup:kotlinpoet:$kotlinpoetVersion")
    implementation("com.squareup:kotlinpoet-metadata:$kotlinpoetVersion")
    implementation("com.squareup:kotlinpoet-metadata-specs:$kotlinpoetVersion")
    implementation("com.squareup:kotlinpoet-classinspector-elements:$kotlinpoetVersion")
    implementation("com.squareup:kotlinpoet-classinspector-reflective:$kotlinpoetVersion")

    implementation("com.github.tschuchortdev:kotlin-compile-testing:1.3.6")
    implementation("javax.enterprise:cdi-api:2.0.SP1")

    testImplementation(platform("org.junit:junit-bom:5.7.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.mockk:mockk:1.10.2")
    testImplementation("org.jooq:joor:0.9.13")

}

val myGroupId = "com.maju.proxy"
val myArtifactId = "proxy-generator"
val myVersion = "1.0.9"

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


application {
    mainClassName = "com.maju.Mainkt"
}


tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}
