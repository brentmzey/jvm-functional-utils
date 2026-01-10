# Setup Checklist for Publishing

This is your step-by-step checklist to get `jvm-functional-utils` published to Maven Central.

## ✅ Already Completed

- [x] Created Kotlin Multiplatform project structure
- [x] Configured Gradle build with KMP support (JVM + JS targets)
- [x] Added MIT License with your name (Brent Zey)
- [x] Set up GitHub Actions CI/CD pipeline
- [x] Created sample functional utilities and tests
- [x] Verified build and tests pass successfully
- [x] Configured Maven Central publishing in build.gradle.kts
- [x] Added comprehensive documentation (README, CONTRIBUTING, PUBLISHING)
- [x] Set up .sdkmanrc for Java 21 (GraalVM) and Kotlin 2.1.0
- [x] Added proper .gitignore for the project

## 📝 TODO: Before First Publish

### 1. Update Build Configuration
Open `lib/build.gradle.kts` and update:
```kotlin
group = "io.github.YOUR-GITHUB-USERNAME"  // Line 8
version = "1.0.0"  // Line 9 - remove -SNAPSHOT for first release
```

And in the `publishing` section (lines 68-104):
```kotlin
url.set("https://github.com/YOUR-GITHUB-USERNAME/jvm-functional-utils")
// Update developer info
id.set("YOUR-GITHUB-USERNAME")
name.set("Brent Zey")  // Or your preferred name
email.set("your.email@example.com")
// Update SCM URLs
connection.set("scm:git:git://github.com/YOUR-GITHUB-USERNAME/jvm-functional-utils.git")
developerConnection.set("scm:git:ssh://github.com/YOUR-GITHUB-USERNAME/jvm-functional-utils.git")
url.set("https://github.com/YOUR-GITHUB-USERNAME/jvm-functional-utils")
```

### 2. Create Sonatype OSSRH Account
See detailed instructions in [PUBLISHING.md](PUBLISHING.md#1-create-a-sonatype-ossrh-account)

- [ ] Create JIRA account at https://issues.sonatype.org/
- [ ] Create "New Project" ticket requesting `io.github.YOUR-USERNAME` namespace
- [ ] Wait for approval (~2 business days)
- [ ] Verify GitHub ownership as instructed

### 3. Generate GPG Keys
See detailed instructions in [PUBLISHING.md](PUBLISHING.md#2-generate-gpg-keys-for-signing)

- [ ] Generate GPG key: `gpg --gen-key`
- [ ] Note your key ID: `gpg --list-keys`
- [ ] Upload to key server: `gpg --keyserver keyserver.ubuntu.com --send-keys KEYID`
- [ ] Export private key as base64: `gpg --export-secret-keys KEYID | base64 > private-key.txt`

### 4. Configure GitHub Secrets
In your GitHub repo: Settings → Secrets and variables → Actions

- [ ] Add `OSSRH_USERNAME` (your Sonatype JIRA username)
- [ ] Add `OSSRH_PASSWORD` (your Sonatype JIRA password)
- [ ] Add `SIGNING_KEY_ID` (your GPG key ID - last 8 chars)
- [ ] Add `SIGNING_KEY` (base64-encoded private key from step 3)
- [ ] Add `SIGNING_PASSWORD` (your GPG key passphrase)

⚠️ Keep these secure! Never commit them to git.

### 5. Push to GitHub
If you haven't already:

```bash
# Initialize git repo (if not done)
git init
git add .
git commit -m "Initial commit: Kotlin Multiplatform functional utils"

# Create GitHub repo at https://github.com/new
# Then push:
git remote add origin https://github.com/YOUR-USERNAME/jvm-functional-utils.git
git branch -M main
git push -u origin main
```

### 6. Test the Build
Before publishing, verify everything works:

```bash
# Use the SDKMAN versions
sdk env install

# Build and test
./gradlew clean build test

# Verify all tests pass
```

### 7. Create a Release

```bash
# Update version in lib/build.gradle.kts to remove -SNAPSHOT
# Example: version = "1.0.0"

git add lib/build.gradle.kts
git commit -m "Prepare release 1.0.0"
git push

# Create and push a tag
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

GitHub Actions will automatically build, test, and publish to Maven Central staging.

### 8. Release on Maven Central

- [ ] Go to https://s01.oss.sonatype.org/
- [ ] Log in with OSSRH credentials
- [ ] Click "Staging Repositories"
- [ ] Find your repository (io.github.YOUR-USERNAME)
- [ ] Click "Close" and wait for validation (5-10 mins)
- [ ] Review validation results
- [ ] Click "Release" to publish
- [ ] Wait ~2 hours for sync to Maven Central

### 9. Verify Publication

- [ ] Check Maven Central: https://search.maven.org/ (search for your artifact)
- [ ] Test in a sample project by adding your library as a dependency
- [ ] Update README.md with correct Maven/Gradle coordinates

## 🎉 Post-Publication

### Announce Your Library
- [ ] Update README badges (add Maven Central version badge)
- [ ] Create a GitHub release with release notes
- [ ] Share on social media (Twitter, Reddit, etc.)
- [ ] Add to Awesome Kotlin list if applicable

### Maintain
- [ ] Set up issue templates
- [ ] Consider adding Code of Conduct
- [ ] Monitor issues and PRs
- [ ] Plan future releases

## 📚 Resources

- [README.md](README.md) - Project overview and usage
- [PUBLISHING.md](PUBLISHING.md) - Detailed publishing guide
- [CONTRIBUTING.md](CONTRIBUTING.md) - Contribution guidelines
- [LICENSE](LICENSE) - MIT License
- [.sdkmanrc](.sdkmanrc) - SDKMAN version configuration

## 🔧 Development Commands

```bash
# Build
./gradlew build

# Run tests
./gradlew test
./gradlew jvmTest  # JVM only
./gradlew jsTest   # JS only

# Publish to local Maven repo (for testing)
./gradlew publishToMavenLocal

# Publish to Maven Central (requires secrets)
./gradlew publishAllPublicationsToOSSRHRepository

# Check for outdated dependencies
./gradlew dependencyUpdates

# Clean build
./gradlew clean
```

## ❓ Need Help?

- Check [PUBLISHING.md](PUBLISHING.md) for troubleshooting
- Review Sonatype documentation: https://central.sonatype.org/
- Open an issue if you encounter problems

---

Good luck with your library! 🚀
