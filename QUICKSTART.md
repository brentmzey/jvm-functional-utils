# Quick Start Guide

## 🎯 The One Command You Need

### After cloning or changing code:
```bash
./gradlew clean build
```

**That's it!** This builds everything, runs all tests, and verifies quality.

---

## 📦 What Do I Need to Publish RIGHT NOW?

### **Option 1: Local Testing (Install to your machine)**
```bash
# Install to ~/.m2/repository for local projects to use
./gradlew publishToMavenLocal

# Use in other projects:
# dependencies { implementation("io.github.yourusername:jvm-functional-utils:1.0.0-SNAPSHOT") }
```

### **Option 2: Publish to Maven Central (Public Release)**

#### Prerequisites (One-time setup - takes 2-3 business days):
1. **Sonatype Account**: Create JIRA ticket at https://issues.sonatype.org/
   - Request namespace: `io.github.YOUR-GITHUB-USERNAME`
   - Wait for approval (~2 business days)

2. **GPG Keys**: Sign artifacts
   ```bash
   gpg --gen-key
   gpg --keyserver keyserver.ubuntu.com --send-keys YOUR-KEY-ID
   gpg --export-secret-keys YOUR-KEY-ID | base64 > private-key.txt
   ```

3. **GitHub Secrets**: Add to repo Settings → Secrets → Actions
   - `OSSRH_USERNAME` - Your Sonatype JIRA username
   - `OSSRH_PASSWORD` - Your Sonatype JIRA password
   - `SIGNING_KEY_ID` - Your GPG key ID (last 8 chars)
   - `SIGNING_KEY` - Contents of private-key.txt
   - `SIGNING_PASSWORD` - Your GPG key passphrase

#### After Setup, Publishing Takes 5 Minutes:

1. **Update version** in `lib/build.gradle.kts`:
   ```kotlin
   version = "1.0.0"  // Remove -SNAPSHOT
   ```

2. **Create release tag**:
   ```bash
   git add lib/build.gradle.kts
   git commit -m "Release 1.0.0"
   git tag -a v1.0.0 -m "Release version 1.0.0"
   git push origin main
   git push origin v1.0.0
   ```

3. **GitHub Actions publishes automatically!**
   - Builds all platforms
   - Runs tests
   - Publishes to Maven Central staging

4. **Release the staged artifacts**:
   - Login: https://s01.oss.sonatype.org/
   - Find your staging repository
   - Click "Close" (validates artifacts - takes 5 min)
   - Click "Release" (publishes to Maven Central)
   - Wait ~2 hours for sync to https://search.maven.org/

---

## 🔄 Daily Development Workflow

```bash
# 1. Make your code changes
vim lib/src/commonMain/kotlin/io/github/functional/IO.kt

# 2. Build and test
./gradlew clean build

# 3. Check results
echo "✅ All tests passed if you see BUILD SUCCESSFUL"

# 4. View coverage report (optional)
open lib/build/reports/kover/html/index.html
```

---

## ⚡ Common Commands

| What | Command |
|------|---------|
| **Build everything** | `./gradlew clean build` |
| **Run tests only** | `./gradlew test` |
| **Install locally** | `./gradlew publishToMavenLocal` |
| **Check coverage** | `./gradlew koverHtmlReport` |
| **Quality checks** | `./run-quality-checks.sh` |

---

## 🚨 I Just Want to Test It Locally NOW

```bash
# 1. Build it
./gradlew clean build

# 2. Install to local Maven repo
./gradlew publishToMavenLocal

# 3. Use it in another project
# In your other project's build.gradle.kts:
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("io.github.yourusername:jvm-functional-utils-jvm:1.0.0-SNAPSHOT")
}
```

---

## 📚 More Details

- **Full build guide**: [BUILDING.md](BUILDING.md)
- **Publishing guide**: [PUBLISHING.md](PUBLISHING.md)
- **Contributing**: [CONTRIBUTING.md](CONTRIBUTING.md)

---

## 💡 TL;DR

**To develop:**
```bash
./gradlew clean build
```

**To test locally:**
```bash
./gradlew publishToMavenLocal
```

**To publish publicly:**
- Set up Sonatype account (one-time, 2 days)
- Create git tag
- GitHub Actions does the rest
