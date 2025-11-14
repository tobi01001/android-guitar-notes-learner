# Contributing to Android Guitar Notes Learner

Thank you for your interest in contributing to Android Guitar Notes Learner! We welcome contributions from the community.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [How to Contribute](#how-to-contribute)
- [Coding Standards](#coding-standards)
- [Testing](#testing)
- [Pull Request Process](#pull-request-process)

## Code of Conduct

This project adheres to a Code of Conduct that all contributors are expected to follow. Please read [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) before contributing.

## Getting Started

1. Fork the repository on GitHub
2. Clone your fork locally
3. Create a new branch for your contribution
4. Make your changes
5. Test your changes thoroughly
6. Submit a pull request

## Development Setup

### Prerequisites

- Android Studio (latest stable version recommended)
- JDK 17
- Android SDK with API level 34+
- An Android device or emulator for testing (microphone required for audio features)

### Building the Project

```bash
# Clone your fork
git clone https://github.com/YOUR_USERNAME/android-guitar-notes-learner.git
cd android-guitar-notes-learner

# Build the project
./gradlew assembleDebug

# Run tests
./gradlew test

# Check code formatting
./gradlew ktlintCheck

# Auto-fix formatting issues
./gradlew ktlintFormat

# Run Android Lint
./gradlew lint
```

## How to Contribute

### Reporting Bugs

- Use the GitHub issue tracker
- Check if the issue already exists
- Provide detailed information:
  - Steps to reproduce
  - Expected vs actual behavior
  - Device/emulator information
  - Android version
  - Relevant logs or screenshots

### Suggesting Enhancements

- Open an issue with the "enhancement" label
- Describe the feature and its benefits
- Explain the use case
- Consider implementation complexity

### Code Contributions

1. **Find or create an issue** - Discuss your planned changes first
2. **Fork and branch** - Create a feature branch from `main`
3. **Write code** - Follow our coding standards
4. **Add tests** - All new features should have tests
5. **Update documentation** - Update README or docs as needed
6. **Submit PR** - Reference the issue in your PR description

## Coding Standards

### Kotlin Style

This project uses [ktlint](https://github.com/pinterest/ktlint) for code formatting. All code must pass ktlint checks.

```bash
# Check formatting
./gradlew ktlintCheck

# Auto-fix formatting
./gradlew ktlintFormat
```

### Code Guidelines

- Follow MVVM architecture pattern
- Use Jetpack Compose for UI
- Prefer Kotlin coroutines for asynchronous operations
- Use StateFlow for state management
- Write meaningful commit messages
- Keep functions small and focused
- Add KDoc comments for public APIs
- Use descriptive variable and function names

### Project Structure

```
app/src/main/java/com/androidguitarnotes/app/
├── audio/          # Audio processing and pitch detection
├── permissions/    # Permission handling
├── practice/       # Practice session logic
├── settings/       # App settings
├── tuner/          # Tuner functionality
└── ui/            # Shared UI components
```

## Testing

### Unit Tests

- Write unit tests for all new functionality
- Aim for high test coverage
- Use JUnit 4 and MockK for testing
- Run tests before submitting PR:

```bash
./gradlew test
```

### Manual Testing

- Test on real devices when possible
- Verify microphone functionality works correctly
- Test on different Android versions if possible
- Check UI responsiveness

## Pull Request Process

1. **Update documentation** - Update README.md if needed
2. **Run all checks** - Ensure tests pass and code is formatted
3. **Write a clear PR description**:
   - What changes were made
   - Why they were made
   - How to test them
   - Reference related issues
4. **Request review** - Wait for maintainer review
5. **Address feedback** - Make requested changes promptly
6. **Squash commits** - Clean up commit history if requested

### PR Checklist

- [ ] Code builds successfully
- [ ] All tests pass
- [ ] ktlint checks pass
- [ ] Android Lint checks pass
- [ ] Documentation updated
- [ ] Commit messages are clear
- [ ] PR description is complete

## Questions?

If you have questions about contributing, feel free to:
- Open an issue with the "question" label
- Start a discussion in GitHub Discussions

Thank you for contributing to Android Guitar Notes Learner!
