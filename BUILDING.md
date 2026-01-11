# Building JVM Functional Utils

Comprehensive guide for building, testing, and working with this library.

## 🛠️ Prerequisites

### Required
- **Java**: GraalVM 21+ or OpenJDK 21+ (Java 8+ target compatibility)
- **Gradle**: 8.5+ (included via wrapper)

### Optional
- **SDKMAN**: For automatic version management (recommended)
- **GraalVM**: For native image compilation

## 📦 Quick Start

### Using SDKMAN (Recommended)

```bash
# Clone the repository
git clone https://github.com/yourusername/jvm-functional-utils.git
cd jvm-functional-utils

# Install correct Java/Kotlin versions automatically
sdk env install

# Build everything
./gradlew clean build

# Run all tests
./gradlew test
```

### Manual Setup

```bash
# Ensure you have Java 21+
java -version

# Build
./gradlew clean build

# Run tests
./gradlew test
```

## 🏗️ Build Commands

### Essential Commands

| Command | Description |
|---------|-------------|
| `./gradlew clean` | Clean build artifacts |
| `./gradlew build` | Compile all platforms (JVM, JS, Native) |
| `./gradlew test` | Run all tests |
| `./gradlew allTests` | Explicitly run all platform tests |
| `./run-quality-checks.sh` | Run tests + coverage + PMD |

### Platform-Specific Builds

| Command | Description |
|---------|-------------|
| `./gradlew jvmJar` | Build JVM JAR only |
| `./gradlew jsJar` | Build JavaScript library |
| `./gradlew linkReleaseExecutableLinuxX64` | Build Linux native binary |
| `./gradlew linkReleaseExecutableMacosX64` | Build macOS Intel binary |
| `./gradlew linkReleaseExecutableMacosArm64` | Build macOS ARM binary |
| `./gradlew linkReleaseExecutableMingwX64` | Build Windows binary |

### Testing

| Command | Description |
|---------|-------------|
| `./gradlew jvmTest` | Run JVM tests only |
| `./gradlew jsTest` | Run JavaScript tests |
| `./gradlew linuxX64Test` | Run Linux native tests |
| `./gradlew koverHtmlReport` | Generate coverage report |
| `./gradlew koverVerify` | Verify ≥90% coverage |

### Code Quality

| Command | Description |
|---------|-------------|
| `./gradlew pmdMain` | Run PMD static analysis |
| `./gradlew koverHtmlReport` | Generate test coverage report |
| `./gradlew check` | Run all quality checks |

### Publishing

| Command | Description |
|---------|-------------|
| `./gradlew publishToMavenLocal` | Install to local Maven repo (~/.m2) |
| `./gradlew publish` | Publish to remote Maven repository |

## 🔧 GraalVM Native Image

### Building Native Libraries

This library includes GraalVM native-image metadata, so applications using it can compile to native binaries:

```bash
# Your application build.gradle.kts
plugins {
    id("org.graalvm.buildtools.native") version "0.10.3"
}

dependencies {
    implementation("io.github.yourusername:jvm-functional-utils:1.0.0")
}
```

Then build your native binary:

```bash
# Using Gradle plugin
./gradlew nativeCompile

# Or directly with native-image
native-image -jar your-app.jar -o your-app-native
```

### Native Image Benefits

- ⚡ **Fast startup**: <50ms cold start
- 💾 **Low memory**: ~10MB heap vs ~100MB JVM
- 📦 **Single binary**: No JVM installation required
- 🔒 **Security**: Reduced attack surface

### Configuration

The library ships with pre-configured native-image settings in:
```
lib/src/jvmMain/resources/META-INF/native-image/
  io.github.functional/jvm-functional-utils/native-image.properties
```

No additional configuration needed!

## 📊 Quality Metrics

### View Reports

```bash
# After running tests/checks
open lib/build/reports/kover/html/index.html    # Coverage report
open lib/build/reports/pmd/main.html            # PMD violations
open lib/build/reports/tests/test/index.html    # Test results
```

### Thresholds

- **Test Coverage**: ≥90% (currently 98.46%)
- **PMD Violations**: 0 (priority 1-5)
- **Test Success**: 100% (106/106 tests passing)

## 🐛 Troubleshooting

### Build Fails

```bash
# Clean and rebuild
./gradlew clean build --refresh-dependencies

# Verbose output
./gradlew build --info

# Debug mode
./gradlew build --debug
```

### Java Version Issues

```bash
# Using SDKMAN
sdk env install

# Or set manually
export JAVA_HOME=/path/to/graalvm-21
```

### Test Failures

```bash
# Run specific platform tests
./gradlew jvmTest --tests "io.github.functional.OptionalUtilsTest"

# Run with stack traces
./gradlew test --stacktrace
```

### Native Image Fails

```bash
# Enable verbose output
native-image -jar app.jar --verbose

# Check reflection config
native-image -jar app.jar -H:+PrintAnalysisCallTree
```

## 🔍 Development Workflow

### 1. Make Changes

```bash
# Edit code in lib/src/
vim lib/src/commonMain/kotlin/io/github/functional/IO.kt
```

### 2. Run Tests

```bash
# Quick test
./gradlew jvmTest

# All platforms
./gradlew test
```

### 3. Check Coverage

```bash
./gradlew koverHtmlReport
open lib/build/reports/kover/html/index.html
```

### 4. Static Analysis

```bash
./gradlew pmdMain
open lib/build/reports/pmd/main.html
```

### 5. Full Quality Check

```bash
./run-quality-checks.sh
```

## 📦 Project Structure

```
jvm-functional-utils/
├── lib/                                 # Main library module
│   ├── build.gradle.kts                # Build configuration
│   └── src/
│       ├── commonMain/kotlin/          # Multiplatform Kotlin
│       ├── commonTest/kotlin/          # Multiplatform tests
│       ├── jvmMain/
│       │   ├── java/                   # Java code
│       │   └── resources/
│       │       └── META-INF/native-image/  # GraalVM config
│       └── jvmTest/java/              # Java tests
├── gradle/                             # Gradle wrapper
├── gradlew                             # Unix wrapper script
├── gradlew.bat                         # Windows wrapper script
├── settings.gradle.kts                 # Gradle settings
├── .sdkmanrc                           # SDKMAN version config
├── run-quality-checks.sh              # QA automation script
├── README.md                           # Project overview
├── BUILDING.md                         # This file
├── CONTRIBUTING.md                     # Contribution guide
└── PUBLISHING.md                       # Maven Central publishing
```

## 🚀 CI/CD

GitHub Actions automatically:
- Builds all platforms (JVM, JS, Native)
- Runs all tests
- Generates coverage reports
- Runs PMD static analysis
- Publishes to Maven Central (on release tags)

See `.github/workflows/` for pipeline configuration.

## 💡 Tips

- **Use SDKMAN**: Automates Java/Kotlin version management
- **Run quality checks locally**: Catch issues before pushing
- **Test all platforms**: Native bugs can be platform-specific
- **Check reports**: HTML reports are easier to read than console
- **Incremental builds**: Gradle caches, so rebuilds are fast

## 📚 Additional Resources

- [README.md](README.md) - Library usage guide
- [CONTRIBUTING.md](CONTRIBUTING.md) - Contribution guidelines
- [PUBLISHING.md](PUBLISHING.md) - Publishing to Maven Central
- [GraalVM Native Image](https://www.graalvm.org/native-image/) - Official docs
- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) - KMP docs

---

**Questions?** Open an issue on GitHub!
