import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.20"
    kotlin("kapt") version "1.4.20"

    `java-library`
    `maven-publish`
    `signing`

    id("io.codearte.nexus-staging") version "0.21.2"
    id("de.marcphilipp.nexus-publish") version "0.4.0"
}

group = "io.neighbwang"
version = "1.0.0"


repositories {
    mavenLocal()
    mavenCentral()
    google()
    jcenter()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

val sourcesJar by tasks.registering(Jar::class) {
    dependsOn(JavaPlugin.CLASSES_TASK_NAME)
    classifier = "sources"
    from(sourceSets.main.get().allSource)
}

val javadocJar by tasks.registering(Jar::class) {
    dependsOn(JavaPlugin.JAVADOC_TASK_NAME)
    classifier = "javadoc"
    from(tasks["javadoc"])
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-Xinline-classes")
}

val OSSRH_USERNAME = project.properties["OSSRH_USERNAME"]?.toString() ?: System.getenv("OSSRH_USERNAME")
val OSSRH_PASSWORD = project.properties["OSSRH_PASSWORD"]?.toString() ?: System.getenv("OSSRH_PASSWORD")

nexusStaging {
    packageGroup = "io.neighbwang"
    username = OSSRH_USERNAME
    password = OSSRH_PASSWORD
    numberOfRetries = 50
    delayBetweenRetriesInMillis = 3000
}

nexusPublishing {
    repositories {
        sonatype {
            username.set(OSSRH_USERNAME)
            password.set(OSSRH_PASSWORD)
        }
    }
}

publishing {
    repositories {
        maven {
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
        }
    }
    publications {
        register("mavenJava", MavenPublication::class) {
            groupId = "${project.group}"
            artifactId = project.name
            version = "${project.version}"

            from(components["java"])

            artifact(sourcesJar.get())
            artifact(javadocJar.get())

            pom.withXml {
                asNode().apply {
                    appendNode("name", project.name)
                    appendNode("url", "https://github.com/neighbwang/${project.name}")
                    appendNode("description", project.description ?: project.name)
                    appendNode("scm").apply {
                        appendNode("connection", "scm:git:git://github.com/neighbwang/${project.name}.git")
                        appendNode("developerConnection", "scm:git:git@github.com:neighbwang/${project.name}.git")
                        appendNode("url", "https://github.com/neighbwang/${project.name}")
                    }
                    appendNode("licenses").apply {
                        appendNode("license").apply {
                            appendNode("name", "GPL License")
                            appendNode("url", "https://www.gnu.org/licenses/gpl-3.0.txt")
                        }
                    }
                    appendNode("developers").apply {
                        appendNode("developer").apply {
                            appendNode("id", "neighbwang")
                            appendNode("name", "Wang Zhiguo")
                            appendNode("email", "wangzhiguo13120140@163.com")
                        }
                    }
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}
