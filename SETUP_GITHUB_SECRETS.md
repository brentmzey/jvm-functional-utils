# GitHub Secrets Setup Guide

Follow these steps to properly configure your GitHub Actions secrets for Maven Central publishing.

## Prerequisites

- You have a PGP key pair for signing
- You have a Sonatype (Maven Central) account
- You have a Sonatype token generated

---

## Step 1: Export Your PGP Private Key

First, find your key ID:

```bash
gpg --list-secret-keys --keyid-format=long
```

You'll see output like:
```
sec   rsa4096/ABCD1234EFGH5678 2024-01-01 [SC]
      1234567890ABCDEF1234567890ABCDEF12345678
uid                 [ultimate] Your Name <your.email@example.com>
ssb   rsa4096/12345678 2024-01-01 [E]
```

The key ID is the 8-character code after the `/` (e.g., `12345678`).

Export the private key:

```bash
# Replace YOUR_KEY_ID with your actual key ID
gpg --armor --export-secret-keys YOUR_KEY_ID > private-key.asc
```

---

## Step 2: Generate Base64-Encoded Values

Use the helper script:

```bash
./encode-key-for-github.sh private-key.asc
```

This will output:
- **SIGNING_KEY_ID**: The 8-character key ID
- **SIGNING_KEY**: The base64-encoded private key (long string)
- **SIGNING_PASSWORD**: Reminder to use your key passphrase

**Copy these values** - you'll need them in the next step.

---

## Step 3: Set GitHub Secrets

1. Go to your GitHub repository
2. Click **Settings** (top right)
3. In the left sidebar, click **Secrets and variables** → **Actions**
4. Click **New repository secret**

Create these **5 secrets** one by one:

### Secret 1: SIGNING_KEY_ID
- **Name**: `SIGNING_KEY_ID`
- **Value**: The 8-character key ID from Step 2
- Example: `12345678`

### Secret 2: SIGNING_KEY
- **Name**: `SIGNING_KEY`
- **Value**: The entire base64 string from Step 2
- ⚠️ **Important**: Copy the ENTIRE string (it's very long, ~2000+ characters)
- ⚠️ No spaces, no newlines, just one continuous string

### Secret 3: SIGNING_PASSWORD
- **Name**: `SIGNING_PASSWORD`
- **Value**: The passphrase you use to unlock your PGP key
- This is what you enter when you sign things locally with `gpg`

### Secret 4: MAVEN_CENTRAL_USERNAME
- **Name**: `MAVEN_CENTRAL_USERNAME`
- **Value**: Your Sonatype JIRA username
- This is your login for https://s01.oss.sonatype.org or https://oss.sonatype.org

### Secret 5: MAVEN_CENTRAL_TOKEN
- **Name**: `MAVEN_CENTRAL_TOKEN`
- **Value**: Your Sonatype token (NOT your password)
- Generate at: https://s01.oss.sonatype.org/#profile → User Token

---

## Step 4: Verify Secrets Are Set

After creating all 5 secrets, you should see them listed in the Secrets page:
- ✅ SIGNING_KEY_ID
- ✅ SIGNING_KEY
- ✅ SIGNING_PASSWORD
- ✅ MAVEN_CENTRAL_USERNAME
- ✅ MAVEN_CENTRAL_TOKEN

---

## Step 5: Test the Setup

After setting the secrets, push your changes:

```bash
git add -A
git commit -m "Fix: Simplify PGP signing for CI compatibility"
git push
```

Then check the GitHub Actions run. You should see:
```
✓ Key format: Base64 encoded, decoding...
✓ Signing configured successfully
```

---

## Troubleshooting

### "Could not read PGP secret key"
- Your SIGNING_KEY might not be properly base64 encoded
- Run `./encode-key-for-github.sh` again and copy the ENTIRE output

### "Wrong passphrase"
- Check your SIGNING_PASSWORD matches your local passphrase
- Test locally: `echo "test" | gpg --clearsign --local-user YOUR_KEY_ID`

### "Key ID mismatch"
- The build will auto-detect, but verify your SIGNING_KEY_ID is correct
- Run: `gpg --list-packets private-key.asc | grep keyid | head -1`

### Still failing?
- See SIGNING_TROUBLESHOOTING.md for detailed debugging
- The build has fallback mechanisms that should work even if format is slightly off

---

## Security Note

⚠️ **Delete the private-key.asc file after setup:**

```bash
rm -f private-key.asc
```

Your private key is now stored securely in GitHub Secrets (encrypted at rest).

---

## Alternative: ASCII Armored Key

If you prefer NOT to use base64 encoding:

```bash
# Copy the ASCII armored key
cat private-key.asc | pbcopy  # macOS
cat private-key.asc | xclip -selection clipboard  # Linux (needs xclip installed)
```

Paste the ENTIRE key (including BEGIN/END markers) into SIGNING_KEY secret.

**Note**: This may have issues if GitHub transforms newlines. Base64 is more reliable.
