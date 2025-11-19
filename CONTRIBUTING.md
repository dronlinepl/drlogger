# Contributing to DR-Logger

Thank you for your interest in contributing to DR-Logger! This document provides guidelines and instructions for contributing.

## Getting Started

1. **Fork the repository** on GitHub
2. **Clone your fork** locally:
   ```bash
   git clone https://github.com/dronlinepl/drlogger-library.git
   cd drlogger-library
   ```
3. **Add upstream remote**:
   ```bash
   git remote add upstream https://github.com/ORIGINAL_OWNER/drlogger-library.git
   ```

## Development Setup

### Prerequisites

- JDK 17 or higher
- Android SDK (for Android targets)
- Xcode (for iOS/macOS targets, macOS only)
- Linux: `libsystemd-dev` and `gcc-multilib` packages

### Building

```bash
# Set Java 17
export JAVA_HOME=/path/to/java-17

# Build the project
./gradlew build

# Run tests
./gradlew test
```

## Making Changes

1. **Create a feature branch**:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make your changes** following the code style guidelines below

3. **Write or update tests** for your changes

4. **Run tests** to ensure everything works:
   ```bash
   ./gradlew test
   ```

5. **Commit your changes**:
   ```bash
   git add .
   git commit -m "Add feature: description of your feature"
   ```

## Code Style Guidelines

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Add KDoc comments for public APIs
- Keep functions focused and concise
- Use Kotlin idioms when appropriate

## Testing

- Write unit tests for new features
- Ensure all existing tests pass
- Test on multiple platforms when possible
- Include edge cases in your tests

## Documentation

- Update README.md if you change user-facing features
- Add KDoc comments to public APIs
- Update CHANGELOG.md with your changes (if applicable)

## Submitting a Pull Request

1. **Push to your fork**:
   ```bash
   git push origin feature/your-feature-name
   ```

2. **Create a Pull Request** on GitHub with:
   - Clear title describing the change
   - Detailed description of what and why
   - Reference to any related issues
   - Screenshots/examples if applicable

3. **Respond to feedback** from maintainers

## Pull Request Checklist

Before submitting, ensure:

- [ ] Code follows project style guidelines
- [ ] All tests pass (`./gradlew test`)
- [ ] New code has appropriate test coverage
- [ ] Documentation is updated if needed
- [ ] Commit messages are clear and descriptive
- [ ] No merge conflicts with main branch

## Reporting Issues

When reporting bugs, please include:

- **Description**: Clear description of the issue
- **Steps to reproduce**: Detailed steps to trigger the bug
- **Expected behavior**: What should happen
- **Actual behavior**: What actually happens
- **Environment**: OS, Kotlin version, platform (Android/iOS/JVM/etc.)
- **Code sample**: Minimal code to reproduce the issue

## Feature Requests

We welcome feature requests! Please:

- Check if the feature already exists
- Describe the use case clearly
- Explain why it would be useful
- Provide examples if possible

## Questions?

If you have questions about contributing:

- Open a discussion on GitHub
- Check existing issues and pull requests
- Contact the maintainers

## License

By contributing, you agree that your contributions will be licensed under the MIT License.

## Code of Conduct

- Be respectful and inclusive
- Welcome newcomers and help them learn
- Focus on constructive feedback
- Respect differing viewpoints and experiences

Thank you for contributing to DR-Logger! 🎉
