# Contributing to SilkBlade

First of all, thank you for considering contributing to SilkBlade! We appreciate your time and effort, and we value any contribution, whether it's reporting a bug, suggesting new features, improving documentation, or writing code.

This document provides guidelines and steps for contributing to SilkBlade.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [How Can I Contribute?](#how-can-i-contribute)
  - [Reporting Bugs](#reporting-bugs)
  - [Suggesting Features](#suggesting-features)
  - [Code Contributions](#code-contributions)
- [Development Process](#development-process)
  - [Setting Up the Development Environment](#setting-up-the-development-environment)
  - [Coding Guidelines](#coding-guidelines)
  - [Commit Messages](#commit-messages)
  - [Pull Requests](#pull-requests)
- [Documentation](#documentation)

## Code of Conduct

This project and everyone participating in it is governed by our Code of Conduct. By participating, you are expected to uphold this code. Please report unacceptable behavior to [maintainer@email.com].

## Getting Started

Before you begin:
- Make sure you have a [GitHub account](https://github.com/signup/free)
- Familiarize yourself with [Git and GitHub](https://docs.github.com/en/github/getting-started-with-github)
- Read the [documentation](SilkBlade_Documentation.md) to understand the project structure

## How Can I Contribute?

### Reporting Bugs

This section guides you through submitting a bug report for SilkBlade.

Before creating bug reports, please check the existing issues to avoid duplicates. When you create a bug report, please include as many details as possible:

1. **Use a clear and descriptive title**
2. **Describe the exact steps to reproduce the problem**
3. **Provide specific examples**
4. **Describe the behavior you observed after following the steps**
5. **Explain which behavior you expected to see and why**
6. **Include screenshots if possible**
7. **Include details about your environment**:
   - OS version
   - Java version
   - Graphics card and driver version
   - Any relevant hardware details

### Suggesting Features

This section guides you through submitting a feature suggestion for SilkBlade.

Feature suggestions are tracked as GitHub issues. To suggest a feature:

1. **Use a clear and descriptive title**
2. **Provide a detailed description of the feature**
3. **Explain why this feature would be useful**
4. **Include any relevant examples or mock-ups**
5. **Specify which part of the game would be affected**

### Code Contributions

#### Small Fixes

Small fixes can be submitted directly via a pull request.

#### Larger Contributions

For larger contributions:

1. **First discuss the change** - Open an issue to discuss the feature or change before you invest significant time
2. **Fork the repository**
3. **Create a branch** - Use a descriptive name (`feature/add-new-enemy-type`)
4. **Make your changes**
5. **Submit a pull request**

## Development Process

### Setting Up the Development Environment

1. **Fork the repository**
2. **Clone your fork locally**:
   ```bash
   git clone https://github.com/your-username/silkblade.git
   cd silkblade
   ```
3. **Set up the upstream remote**:
   ```bash
   git remote add upstream https://github.com/original-owner/silkblade.git
   ```
4. **Install development dependencies**:
   ```bash
   ./gradlew build
   ```

### Coding Guidelines

- **Follow the existing code style**
- **Use meaningful variable and method names**
- **Write JavaDoc comments for all public methods and classes**
- **Keep methods focused on a single responsibility**
- **Write unit tests for new functionality**
- **Make sure your code builds without warnings**

### Commit Messages

- Use the present tense ("Add feature" not "Added feature")
- Use the imperative mood ("Move class to..." not "Moves class to...")
- Reference issues and pull requests where appropriate
- Limit the first line to 72 characters
- Consider starting the commit message with an applicable emoji:
  - ✨ `:sparkles:` for new features
  - 🐛 `:bug:` for bug fixes
  - 📝 `:memo:` for documentation
  - ♻️ `:recycle:` for refactoring
  - 🧪 `:test_tube:` for adding tests

### Pull Requests

- **Use a clear and descriptive title**
- **Include the purpose of the PR**
- **Reference any related issues**
- **Update documentation if needed**
- **Make sure all tests pass**
- **Make sure your code is properly formatted**

## Documentation

Good documentation is crucial for the project. When contributing:

- Update the documentation to reflect any changes
- Use clear and consistent language
- Provide code examples when relevant
- Follow the existing documentation format
- Check for spelling and grammar

Thank you for contributing to SilkBlade! 