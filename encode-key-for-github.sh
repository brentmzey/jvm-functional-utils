#!/bin/bash

# This script helps encode your PGP key for GitHub Actions secrets
# Usage: ./encode-key-for-github.sh /path/to/your/private-key.asc

if [ -z "$1" ]; then
    echo "Usage: $0 <path-to-pgp-private-key>"
    echo ""
    echo "This will encode your PGP private key in base64 format suitable for GitHub secrets."
    echo ""
    echo "Example:"
    echo "  $0 ~/.gnupg/private-key.asc"
    echo ""
    echo "To export your key first:"
    echo "  gpg --list-secret-keys  # Find your key ID"
    echo "  gpg --armor --export-secret-keys YOUR_KEY_ID > private-key.asc"
    exit 1
fi

KEY_FILE="$1"

if [ ! -f "$KEY_FILE" ]; then
    echo "Error: File not found: $KEY_FILE"
    exit 1
fi

echo "================================================"
echo "PGP Key Information for GitHub Secrets"
echo "================================================"
echo ""

# Get key ID
echo "1. SIGNING_KEY_ID:"
gpg --list-packets "$KEY_FILE" 2>/dev/null | grep "keyid:" | head -1 | awk '{print $NF}'
echo ""

# Encode key as base64 (single line, no wrapping)
echo "2. SIGNING_KEY (base64 encoded):"
base64 -w 0 "$KEY_FILE" 2>/dev/null || base64 "$KEY_FILE"
echo ""
echo ""

echo "3. SIGNING_PASSWORD:"
echo "   (Enter the passphrase you used when creating the key)"
echo ""

echo "================================================"
echo "GitHub Actions Setup Instructions:"
echo "================================================"
echo "1. Go to your repository settings"
echo "2. Navigate to Secrets and variables > Actions"
echo "3. Create three secrets with the values above:"
echo "   - SIGNING_KEY_ID"
echo "   - SIGNING_KEY"
echo "   - SIGNING_PASSWORD"
echo ""
echo "4. Also set Maven Central credentials:"
echo "   - MAVEN_CENTRAL_USERNAME"
echo "   - MAVEN_CENTRAL_TOKEN"
echo ""
echo "================================================"
