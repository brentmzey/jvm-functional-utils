import java.util.Base64

plugins {
    kotlin("multiplatform") version "2.1.0"
    id("maven-publish")
    id("signing")
    id("org.jetbrains.kotlinx.kover") version "0.9.4"
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
        
        // Detect if key is base64-encoded or ASCII armored
        val signingKey = if (signingKeyRaw.contains("BEGIN PGP")) {
            logger.lifecycle("  Key format: ASCII armored")
            signingKeyRaw
        } else {
            logger.lifecycle("  Key format: Base64 encoded, decoding...")
            val decoded = String(Base64.getDecoder().decode(signingKeyRaw))
            if (!decoded.contains("BEGIN PGP")) {
                logger.error("  ERROR: Decoded key doesn't contain PGP markers!")
                logger.error("  Original key preview: ${signingKeyRaw.take(50)}")
                throw IllegalArgumentException("SIGNING_KEY is not valid - must be base64 encoded PGP key or ASCII armored")
            }
            logger.lifecycle("  Decoded key successfully, found PGP markers")
            decoded
        }
        
        // Try with key ID first
        try {
            useInMemoryPgpKeys(signingKeyId.trim(), signingKey, signingPassword.trim())
            sign(publishing.publications)
            logger.lifecycle("✓ Signing configured successfully with key ID")
        } catch (e: Exception) {
            logger.error("Failed with key ID: ${e.message}")
            logger.lifecycle("  Trying without key ID (auto-detect)...")
            
            // Fallback: let Gradle auto-detect the key ID
            try {
                useInMemoryPgpKeys(signingKey, signingPassword.trim())
                sign(publishing.publications)
                logger.lifecycle("✓ Signing configured with auto-detected key ID")
            } catch (e2: Exception) {
                logger.error("All signing methods failed!")
                logger.error("  With key ID: ${e.message}")
                logger.error("  Auto-detect: ${e2.message}")
                logger.error("")
                logger.error("Troubleshooting:")
                logger.error("  1. Verify SIGNING_KEY is base64 encoded: base64 -i private-key.asc")
                logger.error("  2. Verify SIGNING_KEY_ID matches: gpg --list-packets private-key.asc | grep keyid")
                logger.error("  3. Verify SIGNING_PASSWORD is correct")
                throw e2
            }
        }
    }
}

// GraalVM Native Image configuration (for applications using this library)
// The library includes native-image metadata in META-INF/native-image/
// Applications can use: ./gradlew nativeCompile