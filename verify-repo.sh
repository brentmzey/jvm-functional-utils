#!/bin/bash
# Verify repository structure and quality

echo "🔍 Repository Structure Verification"
echo "═══════════════════════════════════════════════════════════════"
echo ""

echo "📁 Root Directory:"
ls -1 | grep -v "^\." | grep -v "^build$" | grep -v "^gradle" | grep -v "^kotlin-js-store" | sed 's/^/  ✅ /'
echo ""

echo "📦 .agent/ Directory (gitignored):"
if [ -d .agent ]; then
    ls -1 .agent/ | sed 's/^/  ✅ /'
else
    echo "  ❌ .agent/ directory not found"
fi
echo ""

echo "🎯 Quality Checks:"
echo "  Running tests..."
./gradlew test --no-daemon > /dev/null 2>&1 && echo "  ✅ All tests passing" || echo "  ❌ Tests failed"

echo "  Checking coverage..."
./gradlew koverVerify --no-daemon > /dev/null 2>&1 && echo "  ✅ Coverage ≥90%" || echo "  ❌ Coverage below 90%"

echo "  Running PMD..."
./gradlew pmdMain --no-daemon > /dev/null 2>&1 && echo "  ✅ Zero PMD violations" || echo "  ❌ PMD violations found"

echo ""
echo "📊 Coverage Stats:"
./gradlew koverLog --no-daemon 2>&1 | grep "coverage:"

echo ""
echo "✨ Verification complete!"
