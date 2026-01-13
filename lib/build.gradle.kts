plugins {
    kotlin("multiplatform") version "2.1.0"
    id("maven-publish")
    id("signing")
    id("org.jetbrains.kotlinx.kover") version "0.8.3"
    id("pmd")
    id("org.graalvm.buildtools.native") version "0.10.3"
}

group = "com.brentzey.functional"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
    
    jvm {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    
    // Native targets for Kotlin/Native
    linuxX64()
    macosX64()
    macosArm64()
    mingwX64()
    
    js(IR) {
        browser {
            testTask {
                useMocha()
            }
        }
        nodejs()
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
            }
        }
        val jvmTest by getting
        val jsMain by getting
        val jsTest by getting
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

// PMD configuration for Java static analysis
pmd {
    isConsoleOutput = true
    toolVersion = "7.0.0"
    rulesMinimumPriority.set(1)
    ruleSets = listOf()
    ruleSetFiles = files("${rootProject.projectDir}/config/pmd/ruleset.xml")
}

tasks.withType<Pmd> {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    exclude("**/examples/**")
    exclude("**/Library*")
}

// Only run PMD on main source code, not tests
tasks.named("pmdTest") {
    enabled = false
}

// Kover configuration for Kotlin coverage
kover {
    reports {
        filters {
            excludes {
                classes("**/examples/**", "**/Library*")
            }
        }
    }
}

// Skip Kover verification - incompatible with configuration cache
tasks.named("koverVerify") {
    enabled = false
}

// Workaround for Kover configuration cache issue
// Disable Kover for the jvmTest task
afterEvaluate {
    tasks.named("jvmTest") {
        doFirst {
            // Create the kover-agent.args file that Kover expects
            val koverAgentArgsFile = file("${buildDir}/tmp/jvmTest/kover-agent.args")
            koverAgentArgsFile.parentFile.mkdirs()
            koverAgentArgsFile.writeText("")
        }
    }
}

publishing {
    publications {
        withType<MavenPublication> {
            artifactId = when (name) {
                "kotlinMultiplatform" -> "jvm-functional-utils"
                "jvm" -> "jvm-functional-utils-jvm"
                "js" -> "jvm-functional-utils-js"
                else -> "jvm-functional-utils-$name"
            }
            
            pom {
                name.set("JVM Functional Utils")
                description.set("Functional programming utilities for the JVM")
                url.set("https://github.com/brentmzey/jvm-functional-utils")
                
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                
                developers {
                    developer {
                        id.set("brentmzey")
                        name.set("Brent Zey")
                        email.set("brentmzey@users.noreply.github.com")
                    }
                }
                
                scm {
                    connection.set("scm:git:git://github.com/brentmzey/jvm-functional-utils.git")
                    developerConnection.set("scm:git:ssh://github.com/brentmzey/jvm-functional-utils.git")
                    url.set("https://github.com/brentmzey/jvm-functional-utils")
                }
            }
        }
    }
    
    repositories {
        maven {
            name = "CentralPortal"
            url = uri("https://central.sonatype.com/api/v1/publisher")
            credentials {
                username = System.getenv("MAVEN_CENTRAL_USERNAME") ?: project.findProperty("mavenCentralUsername") as String?
                password = System.getenv("MAVEN_CENTRAL_TOKEN") ?: project.findProperty("mavenCentralToken") as String?
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(
        System.getenv("SIGNING_KEY_ID"),
        System.getenv("SIGNING_KEY"),
        System.getenv("SIGNING_PASSWORD")
    )
    sign(publishing.publications)
}

// GraalVM Native Image configuration (for applications using this library)
// The library includes native-image metadata in META-INF/native-image/
// Applications can use: ./gradlew nativeCompile
