# PGP Signing Troubleshooting Guide

## The Problem

Publishing to Maven Central requires PGP signing, but Gradle's signing plugin is notoriously finicky about key formats, especially in CI environments where secrets are stored as environment variables.

## Common Issues

1. **"Could not read PGP secret key"** - Key format issue
2. **"Inappropriate ioctl for device"** - GPG trying to use TTY in CI
3. **Key ID mismatch** - Wrong key ID for the actual key

## The Solution

We've implemented a robust fallback system that:
1. Auto-detects if your key is ASCII armored or base64 encoded
2. Uses in-memory signing (no GPG agent/TTY required)
3. Falls back to auto-detecting the key ID if there's a mismatch

## Recommended Setup: Base64 Encoding

The most reliable format for GitHub secrets is **base64 encoded**:

### Step 1: Export Your PGP Key

```bash
# List your keys to find the ID
gpg --list-secret-keys

# Export the private key (replace YOUR_KEY_ID)
gpg --armor --export-secret-keys YOUR_KEY_ID > private-key.asc
```

### Step 2: Encode for GitHub

Use the provided helper script:

```bash
./encode-key-for-github.sh private-key.asc
```

This will output three values you need.

### Step 3: Set GitHub Secrets

Go to your repository → Settings → Secrets and variables → Actions

Create these secrets:
- `SIGNING_KEY_ID` - The 8-character key ID (e.g., `12345678`)
- `SIGNING_KEY` - The base64-encoded key (long string, no newlines)
- `SIGNING_PASSWORD` - Your key passphrase
- `MAVEN_CENTRAL_USERNAME` - Your Sonatype username
- `MAVEN_CENTRAL_TOKEN` - Your Sonatype token

## Alternative: ASCII Armored Key

If you prefer, you can also store the ASCII armored key directly:

```bash
cat private-key.asc | pbcopy  # macOS
cat private-key.asc | xclip -selection clipboard  # Linux
```

Paste the entire key (including BEGIN/END headers) into the `SIGNING_KEY` secret.

⚠️ **Important**: GitHub may transform newlines. Base64 encoding avoids this issue.

## Verification

After setting up your secrets, the build will show:

```
✓ GPG key imported successfully
✓ Signing configured successfully
```

## Why This Is Hard

Maven Central's requirements are strict:
- Must sign with PGP
- Must upload to Sonatype
- Must have proper POM metadata
- Kotlin Multiplatform makes it worse (multiple artifacts)

You're not alone - this is a known pain point in the ecosystem!

## References

- [Gradle Signing Plugin Docs](https://docs.gradle.org/current/userguide/signing_plugin.html)
- [Sonatype Publishing Guide](https://central.sonatype.org/publish/publish-guide/)
- [GitHub Actions Secrets](https://docs.github.com/en/actions/security-guides/encrypted-secrets)
