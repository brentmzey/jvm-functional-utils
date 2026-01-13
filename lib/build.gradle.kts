import java.util.Base64

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

// Log publishing configuration
logger.lifecycle("Publishing configuration:")
logger.lifecycle("  Group: $group")
logger.lifecycle("  Version: $version")
logger.lifecycle("  Maven Central Username: ${if (System.getenv("MAVEN_CENTRAL_USERNAME") != null) "SET" else "NOT SET"}")
logger.lifecycle("  Maven Central Token: ${if (System.getenv("MAVEN_CENTRAL_TOKEN") != null) "SET" else "NOT SET"}")
logger.lifecycle("  Signing Key ID: ${if (System.getenv("SIGNING_KEY_ID") != null) "SET" else "NOT SET"}")

val signingKeyEnv = System.getenv("SIGNING_KEY")
if (signingKeyEnv != null) {
    val keyPreview = signingKeyEnv.take(50)
    val hasBeginMarker = keyPreview.contains("-----BEGIN")
    val hasBeginPGP = keyPreview.contains("BEGIN PGP")
    logger.lifecycle("  Signing Key: SET (${signingKeyEnv.length} chars)")
    logger.lifecycle("    Preview: ${keyPreview.take(40)}...")
    logger.lifecycle("    Has BEGIN marker: $hasBeginMarker")
    if (hasBeginPGP) {
        logger.lifecycle("    Format: ASCII armored ✓")
    } else {
        logger.lifecycle("    Format: Will add PGP markers")
    }
} else {
    logger.lifecycle("  Signing Key: NOT SET")
}

logger.lifecycle("  Signing Password: ${if (System.getenv("SIGNING_PASSWORD") != null) "SET" else "NOT SET"}")

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
}

signing {
    val signingKeyId = System.getenv("SIGNING_KEY_ID")
    val signingKeyRaw = System.getenv("SIGNING_KEY")
    val signingPassword = System.getenv("SIGNING_PASSWORD")

    if (signingKeyId != null && signingKeyRaw != null && signingPassword != null) {
        // Reconstruct the PGP key to ensure it's in the correct multi-line format.
        // GitHub Actions can mangle newlines or store the key as a single line.
        val keyBody = signingKeyRaw
            .replace("-----BEGIN PGP PRIVATE KEY BLOCK-----", "")
            .replace("-----END PGP PRIVATE KEY BLOCK-----", "")
            .replace("\\s".toRegex(), "")
        
        val signingKey = """
            -----BEGIN PGP PRIVATE KEY BLOCK-----
            
            ${keyBody.chunked(64).joinToString("\n")}
            -----END PGP PRIVATE KEY BLOCK-----
        """.trimIndent()

        logger.lifecycle("  Reconstructed Signing Key Preview:\n${signingKey.take(120)}...")

        try {
            useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
            sign(publishing.publications)
            logger.lifecycle("✓ Signing configured successfully")
        } catch (e: Exception) {
            logger.error("Failed to configure signing: ${e.message}")
            logger.error("The PGP key is likely invalid or malformed even after reconstruction.")
            logger.error("Please verify the SIGNING_KEY secret. It should be an ASCII-armored PGP private key.")
            throw e
        }
    }
}

// GraalVM Native Image configuration (for applications using this library)
// The library includes native-image metadata in META-INF/native-image/
// Applications can use: ./gradlew nativeCompile
