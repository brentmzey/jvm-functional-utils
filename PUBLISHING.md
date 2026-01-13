# Maven Central Publishing Guide

This document provides instructions for publishing `jvm-functional-utils` to Maven Central using the **com.brentzey** namespace.

## Prerequisites

### 1. Sonatype Central Portal Account

✅ **COMPLETED** - Your `com.brentzey` namespace is already verified on Sonatype Maven Central.

You can manage your publications at: https://central.sonatype.com/

### 2. GPG Keys for Signing

Maven Central requires all artifacts to be signed with GPG.

```bash
# Generate a new GPG key pair
gpg --gen-key

# Follow the prompts:
# - Use RSA and RSA
# - Key size: 4096 bits
# - Key validity: 0 (does not expire) or choose an expiration
# - Real name: Your Name
# - Email: your.email@example.com

# List your keys to get the key ID
gpg --list-keys

# Output will look like:
# pub   rsa4096 2026-01-10 [SC]
#       ABCD1234ABCD1234ABCD1234ABCD1234ABCD1234
# uid           [ultimate] Your Name <your.email@example.com>

# The key ID is the last 8 characters of the fingerprint
# Export your public key to a key server
gpg --keyserver keyserver.ubuntu.com --send-keys YOURKEYID

# Export your private key as base64 for GitHub Secrets
gpg --export-secret-keys YOURKEYID | base64 > private-key.txt

# Get your key ID (last 8 characters of fingerprint)
gpg --list-secret-keys --keyid-format SHORT
```

### 3. Configure GitHub Secrets

Add the following secrets to your GitHub repository:
(Settings → Secrets and variables → Actions → New repository secret)

1. **MAVEN_CENTRAL_USERNAME**: Your Sonatype Central Portal username
2. **MAVEN_CENTRAL_TOKEN**: Your Sonatype Central Portal token (generate at https://central.sonatype.com/account)
3. **SIGNING_KEY_ID**: Your GPG key ID (last 8 characters)
4. **SIGNING_KEY**: Your base64-encoded GPG private key (from private-key.txt)
5. **SIGNING_PASSWORD**: The passphrase for your GPG key

⚠️ **Security**: Never commit these values to your repository!

## Project Configuration

The project is configured with:
- **Group ID**: `com.brentzey.functional`
- **Artifact ID**: `jvm-functional-utils`
- **Package**: `com.brentzey.functional`

All source files use the correct package structure matching the Maven Central namespace.

## Publishing Process

### Automatic Publishing (Recommended)

The project uses GitHub Actions for automated publishing:

#### For Release Versions:

1. **Update the version** in `lib/build.gradle.kts` (remove `-SNAPSHOT`):
   ```kotlin
   version = "1.0.0"  // No -SNAPSHOT suffix
   ```

2. **Commit and push** your changes:
   ```bash
   git add lib/build.gradle.kts
   git commit -m "Release version 1.0.0"
   git push origin main
   ```

3. **Create and push a release tag**:
   ```bash
   git tag -a v1.0.0 -m "Release version 1.0.0"
   git push origin v1.0.0
   ```

4. **Monitor the release**:
   - Go to GitHub Actions tab
   - Watch the "Release to Maven Central" workflow
   - It will automatically:
     - Build the project
     - Run all tests
     - Sign the artifacts
     - Publish to Maven Central
     - Create a GitHub Release

5. **Verify publication**:
   - Check https://central.sonatype.com/ (may take 15-30 minutes)
   - Artifacts will sync to Maven Central within 2 hours
   - Search at: https://search.maven.org/search?q=g:com.brentzey.functional

#### For Snapshot Versions:

Snapshots are automatically published on every push to `main` branch:
- Keep `-SNAPSHOT` suffix in version
- Push to main branch
- CI/CD pipeline publishes automatically

### Manual Publishing

If you prefer to publish manually:

```bash
# Make sure all secrets are set as environment variables
export MAVEN_CENTRAL_USERNAME=your-username
export MAVEN_CENTRAL_TOKEN=your-token
export SIGNING_KEY_ID=your-key-id
export SIGNING_KEY=your-base64-key
export SIGNING_PASSWORD=your-passphrase

# Build and publish
./gradlew clean build publishAllPublicationsToCentralPortalRepository
```

## Verification

After publishing, verify your artifact is available:

1. **Sonatype Central Portal**: https://central.sonatype.com/
   - Search for: `com.brentzey.functional`
   
2. **Maven Central Search**: https://search.maven.org/
   - Search for: `g:com.brentzey.functional a:jvm-functional-utils`
   
3. **Direct URL**: https://repo1.maven.org/maven2/com/brentzey/functional/jvm-functional-utils/

Note: It can take up to 2 hours for artifacts to appear on Maven Central after publication.

## Using Your Published Library

Once published, users can add your library as a dependency:

### Gradle (Kotlin DSL)
```kotlin
dependencies {
    implementation("com.brentzey.functional:jvm-functional-utils:1.0.0")
}
```

### Gradle (Groovy)
```groovy
dependencies {
    implementation 'com.brentzey.functional:jvm-functional-utils:1.0.0'
}
```

### Maven
```xml
<dependency>
    <groupId>com.brentzey.functional</groupId>
    <artifactId>jvm-functional-utils-jvm</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Snapshot Releases

For snapshot releases (versions ending in `-SNAPSHOT`):

1. Snapshots are published automatically on every push to main
2. Available at: https://central.sonatype.com/ (Sonatype's snapshot repository)
3. Note: The new Sonatype Central Portal handles snapshots differently than the legacy OSSRH system

## Troubleshooting

### Build Fails with "401 Unauthorized"
- Check that MAVEN_CENTRAL_USERNAME and MAVEN_CENTRAL_TOKEN are correct
- Verify you're using credentials from https://central.sonatype.com/account (not legacy OSSRH)
- Regenerate your token if needed

### Build Fails with Signing Errors
- Verify your GPG key is correctly base64-encoded
- Check that SIGNING_PASSWORD matches your GPG key passphrase
- Ensure SIGNING_KEY_ID is the correct 8-character ID

### "Namespace not verified" or "Group ID not allowed"
- Verify `com.brentzey` namespace is verified at https://central.sonatype.com/
- Ensure group ID in build.gradle.kts is `com.brentzey.functional`

### Artifacts Not Appearing on Maven Central
- Check publication status at https://central.sonatype.com/
- Wait up to 2 hours for synchronization to Maven Central
- Verify on https://search.maven.org/

### Version Mismatch Error in GitHub Actions
- Ensure the version in `lib/build.gradle.kts` matches the git tag
- Example: If tag is `v1.0.0`, version should be `1.0.0` (no -SNAPSHOT)

## Resources

- [Sonatype Central Portal](https://central.sonatype.com/)
- [Central Portal Publishing Guide](https://central.sonatype.org/publish/publish-portal-gradle/)
- [GPG Signing Guide](https://central.sonatype.org/publish/requirements/gpg/)
- [GitHub Actions Publishing](https://docs.github.com/en/actions/publishing-packages/publishing-java-packages-with-gradle)

---

For questions or issues, please open an issue on GitHub.
