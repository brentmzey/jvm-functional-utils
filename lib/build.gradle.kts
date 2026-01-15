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
    val hasNewlines = signingKeyEnv.contains("\n")
    logger.lifecycle("  Signing Key: SET (${signingKeyEnv.length} chars)")
    logger.lifecycle("    Preview: ${keyPreview.take(40)}...")
    logger.lifecycle("    Has BEGIN marker: $hasBeginMarker")
    logger.lifecycle("    Has Newlines: $hasNewlines")
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
                "kotlinMultiplatform" -> "functional-utils"
                "jvm" -> "functional-utils-jvm"
                "js" -> "functional-utils-js"
                else -> "functional-utils-$name"
            }
            
            pom {
                name.set("Functional Utils")
                description.set("Functional programming utilities for Kotlin Multiplatform")
                url.set("https://github.com/brentmzey/functional-utils")
                
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
                    connection.set("scm:git:git://github.com/brentmzey/functional-utils.git")
                    developerConnection.set("scm:git:ssh://github.com/brentmzey/functional-utils.git")
                    url.set("https://github.com/brentmzey/functional-utils")
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
        logger.lifecycle("  Processing Signing Key...")
        
        try {
            // Detect if key is base64-encoded or ASCII armored
            val signingKey = if (signingKeyRaw.contains("BEGIN PGP")) {
                logger.lifecycle("  Key format: ASCII armored")
                signingKeyRaw
            } else {
                logger.lifecycle("  Key format: Base64 encoded, decoding...")
                try {
                    String(Base64.getDecoder().decode(signingKeyRaw))
                } catch (e: Exception) {
                    logger.error("  Failed to decode base64, using as-is")
                    signingKeyRaw
                }
            }
            
            // Use in-memory keys - this works in CI without TTY
            useInMemoryPgpKeys(signingKeyId.trim(), signingKey, signingPassword.trim())
            sign(publishing.publications)
            logger.lifecycle("✓ Signing configured successfully")
        } catch (e: Exception) {
            logger.error("Failed to configure signing: ${e.message}")
            logger.lifecycle("  Trying without key ID (auto-detect)...")
            
            // Last resort: let Gradle auto-detect the key ID
            try {
                val signingKey = if (signingKeyRaw.contains("BEGIN PGP")) {
                    signingKeyRaw
                } else {
                    String(Base64.getDecoder().decode(signingKeyRaw))
                }
                useInMemoryPgpKeys(signingKey, signingPassword.trim())
                sign(publishing.publications)
                logger.lifecycle("✓ Signing configured with auto-detected key ID")
            } catch (e2: Exception) {
                logger.error("All signing methods failed: ${e2.message}")
                logger.error("Please ensure:")
                logger.error("  1. SIGNING_KEY is either ASCII armored or base64 encoded")
                logger.error("  2. SIGNING_KEY_ID matches the key (or remove it)")
                logger.error("  3. SIGNING_PASSWORD is correct")
                throw e2
            }
        }
    }
}

// GraalVM Native Image configuration (for applications using this library)
// The library includes native-image metadata in META-INF/native-image/
// Applications can use: ./gradlew nativeCompile