title: Frequently Asked Questions

# Frequently Asked Questions

**General**

1. [I use variables in dependencies that are influenced by profiles. This is not working in flattened POM. What can I do?](#profileVariables)

## General

<a id="profileVariables"></a>

### I use variables in dependencies that are influenced by profiles. This is not working in flattened POM. What can I do?

In general dynamic dependencies should be avoided. However, you can move the dependency from the direct project dependencies into the dependencies of all relevant profiles.
