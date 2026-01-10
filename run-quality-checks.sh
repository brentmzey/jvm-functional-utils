#!/bin/bash
# Run all quality checks for JVM Functional Utils

echo "🧪 Running comprehensive quality checks..."
echo ""

echo "📦 1. Cleaning build artifacts..."
./gradlew clean --no-daemon > /dev/null 2>&1

echo "✅ 2. Running all tests..."
./gradlew test --no-daemon

echo "📊 3. Generating coverage report..."
./gradlew koverHtmlReport --no-daemon > /dev/null 2>&1

echo "🔍 4. Verifying coverage threshold (90%)..."
./gradlew koverVerify --no-daemon

echo "📈 5. Getting coverage percentage..."
./gradlew koverLog --no-daemon | grep "coverage:"

echo "🔎 6. Running PMD static analysis..."
./gradlew pmdMain --no-daemon

echo ""
echo "✨ All quality checks completed!"
echo ""
echo "📄 View reports:"
echo "   Coverage: lib/build/reports/kover/html/index.html"
echo "   PMD: lib/build/reports/pmd/main.html"
