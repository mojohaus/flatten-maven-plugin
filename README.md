# MojoHaus Flatten Maven Plugin

[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/mojohaus/flatten-maven-plugin.svg?label=License)](https://www.apache.org/licenses/)
[![Maven Central](https://img.shields.io/maven-central/v/org.codehaus.mojo/flatten-maven-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/org.codehaus.mojo/flatten-maven-plugin)
[![GitHub CI](https://github.com/mojohaus/flatten-maven-plugin/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/mojohaus/flatten-maven-plugin/actions/workflows/maven.yml?query=branch%3Amaster)

The Flatten Maven Plugin generates a flattened version of your `pom.xml`, with variables and the
parent relationship resolved and build-only elements removed, and makes Maven install and deploy
that POM instead of the original. It requires Maven 3.9.0 or later and Java 8 or later.

Add it to the build section of the top-level POM and bind the `flatten` goal, as shown on the
[usage page](https://www.mojohaus.org/flatten-maven-plugin/usage.html). The
[plugin site](https://www.mojohaus.org/flatten-maven-plugin/) has the goal reference and examples.

## Contributing

[AGENTS.md](AGENTS.md) records the build gates, how the unit and integration tests are set up,
and the conventions for fixtures and pull requests.

## Releasing

* Make sure `gpg-agent` is running.
* Execute `mvn release:prepare release:perform`

For publishing the site do the following:

```
cd target/checkout
mvn site scm-publish:publish-scm
```

