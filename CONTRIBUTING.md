# Contributing to JVM Functional Utils

Thank you for your interest in contributing to JVM Functional Utils! This document provides guidelines for contributing to the project.

## Code of Conduct

Be respectful, professional, and inclusive in all interactions.

## Getting Started

1. **Fork the repository** on GitHub
2. **Clone your fork** locally:
   ```bash
   git clone https://github.com/yourusername/jvm-functional-utils.git
   cd jvm-functional-utils
   ```
3. **Install dependencies** using SDKMAN:
   ```bash
   sdk env install
   ```
4. **Create a branch** for your changes:
   ```bash
   git checkout -b feature/your-feature-name
   ```

## Development Workflow

### Building

```bash
./gradlew build
```

### Running Tests

```bash
# All platforms
./gradlew allTests

# Specific platforms
./gradlew jvmTest
./gradlew jsTest
```

### Code Style

- Follow Kotlin coding conventions
- Write clear, self-documenting code
- Add comments only when necessary for clarification
- Keep functions small and focused

### Testing

- Write tests for all new functionality
- Ensure all existing tests pass
- Aim for high test coverage
- Test on multiple platforms (JVM, JS) when applicable

## Submitting Changes

1. **Commit your changes** with clear, descriptive messages:
   ```bash
   git commit -m "Add feature: description of feature"
   ```

2. **Push to your fork**:
   ```bash
   git push origin feature/your-feature-name
   ```

3. **Create a Pull Request**:
   - Go to the original repository on GitHub
   - Click "New Pull Request"
   - Select your fork and branch
   - Provide a clear description of your changes
   - Reference any related issues

### Pull Request Guidelines

- Keep PRs focused on a single feature or fix
- Include tests for new functionality
- Update documentation as needed
- Ensure CI/CD passes all checks
- Be responsive to feedback

## Reporting Issues

When reporting issues, please include:

- A clear, descriptive title
- Steps to reproduce the issue
- Expected behavior
- Actual behavior
- Environment details (OS, Java version, etc.)
- Code samples or error messages

## Feature Requests

We welcome feature requests! Please:

- Check if the feature has already been requested
- Clearly describe the feature and its use case
- Explain why it would be valuable

## Questions?

If you have questions, feel free to:
- Open a GitHub issue with the "question" label
- Start a discussion in GitHub Discussions

## License

By contributing, you agree that your contributions will be licensed under the MIT License.

---

Thank you for contributing! 🎉
