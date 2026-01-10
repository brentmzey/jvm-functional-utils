# Maven Central Publishing Guide

This document provides instructions for publishing `jvm-functional-utils` to Maven Central.

## Prerequisites

Before you can publish to Maven Central, you need to complete the following one-time setup:

### 1. Create a Sonatype OSSRH Account

1. Create a JIRA account at https://issues.sonatype.org/
2. Create a new ticket requesting a namespace:
   - **Project**: Community Support - Open Source Project Repository Hosting (OSSRH)
   - **Issue Type**: New Project
   - **Summary**: Request for io.github.YOURUSERNAME namespace
   - **Group Id**: `io.github.YOURUSERNAME`
   - **Project URL**: https://github.com/YOURUSERNAME/jvm-functional-utils
   - **SCM URL**: https://github.com/YOURUSERNAME/jvm-functional-utils.git
3. Wait for approval (usually within 2 business days)
4. Verify ownership of your GitHub account as instructed in the ticket

### 2. Generate GPG Keys for Signing

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

1. **OSSRH_USERNAME**: Your Sonatype JIRA username
2. **OSSRH_PASSWORD**: Your Sonatype JIRA password  
3. **SIGNING_KEY_ID**: Your GPG key ID (last 8 characters)
4. **SIGNING_KEY**: Your base64-encoded GPG private key (from private-key.txt)
5. **SIGNING_PASSWORD**: The passphrase for your GPG key

⚠️ **Security**: Never commit these values to your repository!

### 4. Update build.gradle.kts

Before publishing, update these fields in `lib/build.gradle.kts`:

```kotlin
group = "io.github.YOURUSERNAME"  // Replace with your actual GitHub username
version = "1.0.0"  // Remove -SNAPSHOT for releases

publishing {
    publications {
        withType<MavenPublication> {
            pom {
                name.set("JVM Functional Utils")
                description.set("Functional programming utilities for the JVM")
                url.set("https://github.com/YOURUSERNAME/jvm-functional-utils")
                
                developers {
                    developer {
                        id.set("YOURUSERNAME")
                        name.set("Your Name")
                        email.set("your.email@example.com")
                    }
                }
                
                scm {
                    connection.set("scm:git:git://github.com/YOURUSERNAME/jvm-functional-utils.git")
                    developerConnection.set("scm:git:ssh://github.com/YOURUSERNAME/jvm-functional-utils.git")
                    url.set("https://github.com/YOURUSERNAME/jvm-functional-utils")
                }
            }
        }
    }
}
```

## Publishing Process

### Automatic Publishing (Recommended)

The project is configured with GitHub Actions to automatically publish releases:

1. **Update the version** in `lib/build.gradle.kts` (remove `-SNAPSHOT`)
2. **Commit and push** your changes
3. **Create and push a release tag**:
   ```bash
   git tag -a v1.0.0 -m "Release version 1.0.0"
   git push origin v1.0.0
   ```
4. GitHub Actions will automatically:
   - Build the project
   - Run all tests
   - Sign the artifacts
   - Publish to Maven Central staging
5. **Release the staging repository**:
   - Go to https://s01.oss.sonatype.org/
   - Log in with your OSSRH credentials
   - Click "Staging Repositories"
   - Find your repository (io.github.YOURUSERNAME)
   - Click "Close" and wait for validation
   - Click "Release" to publish to Maven Central
   - Artifacts will be available on Maven Central within 2 hours

### Manual Publishing

If you prefer to publish manually:

```bash
# Make sure all secrets are set as environment variables
export OSSRH_USERNAME=your-username
export OSSRH_PASSWORD=your-password
export SIGNING_KEY_ID=your-key-id
export SIGNING_KEY=your-base64-key
export SIGNING_PASSWORD=your-passphrase

# Build and publish
./gradlew clean build publishAllPublicationsToOSSRHRepository
```

Then follow step 5 above to release from the staging repository.

## Verification

After publishing, you can verify your artifact is available:

1. **Maven Central Search**: https://search.maven.org/
   - Search for: `g:io.github.YOURUSERNAME a:jvm-functional-utils`
2. **Direct URL**: https://repo1.maven.org/maven2/io/github/YOURUSERNAME/jvm-functional-utils/

Note: It can take up to 2 hours for artifacts to appear on Maven Central after release.

## Using Your Published Library

Once published, users can add your library as a dependency:

### Gradle (Kotlin DSL)
```kotlin
dependencies {
    implementation("io.github.YOURUSERNAME:jvm-functional-utils:1.0.0")
}
```

### Gradle (Groovy)
```groovy
dependencies {
    implementation 'io.github.YOURUSERNAME:jvm-functional-utils:1.0.0'
}
```

### Maven
```xml
<dependency>
    <groupId>io.github.YOURUSERNAME</groupId>
    <artifactId>jvm-functional-utils-jvm</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Snapshot Releases

For snapshot releases (versions ending in `-SNAPSHOT`):

1. Snapshots are published automatically on every push to main
2. Available at: https://s01.oss.sonatype.org/content/repositories/snapshots/
3. Users need to add the snapshot repository:

```kotlin
repositories {
    mavenCentral()
    maven {
        url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }
}
```

## Troubleshooting

### Build Fails with "401 Unauthorized"
- Check that your OSSRH_USERNAME and OSSRH_PASSWORD are correct
- Verify you're using your Sonatype JIRA credentials, not GitHub credentials

### Build Fails with Signing Errors
- Verify your GPG key is correctly base64-encoded
- Check that SIGNING_PASSWORD matches your GPG key passphrase
- Ensure SIGNING_KEY_ID is the correct 8-character ID

### "Group ID not allowed"
- Make sure you've completed the Sonatype JIRA ticket process
- Verify the group ID in build.gradle.kts matches your approved namespace

### Artifacts Not Appearing on Maven Central
- Check that you've "Released" the staging repository in Sonatype
- Wait up to 2 hours for synchronization
- Verify on https://search.maven.org/

## Resources

- [Sonatype OSSRH Guide](https://central.sonatype.org/publish/publish-guide/)
- [Publishing with Gradle](https://central.sonatype.org/publish/publish-gradle/)
- [GPG Signing Guide](https://central.sonatype.org/publish/requirements/gpg/)
- [GitHub Actions Publishing](https://docs.github.com/en/actions/publishing-packages/publishing-java-packages-with-gradle)

---

For questions or issues, please open an issue on GitHub.
