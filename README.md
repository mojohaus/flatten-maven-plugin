# MojoHaus Flatten Maven Plugin

This is the [flatten-maven-plugin](http://www.mojohaus.org/flatten-maven-plugin/).

[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/mojohaus/versions-maven-plugin.svg?label=License)](http://www.apache.org/licenses/)
[![Maven Central](https://img.shields.io/maven-central/v/org.codehaus.mojo/flatten-maven-plugin.svg?label=Maven%20Central)](https://search.maven.org/artifact/org.codehaus.mojo/flatten-maven-plugin)
[![GitHub CI](https://github.com/mojohaus/flatten-maven-plugin/actions/workflows/maven.yml/badge.svg)](https://github.com/mojohaus/flatten-maven-plugin/actions/workflows/maven.yml)

## Quickstart

This plugin generates a flattened version of your pom.xml and makes maven to install and deploy this one instead of the original pom.xml.

Please refer to the [Usage page](https://www.mojohaus.org/flatten-maven-plugin/usage.html) for simple example.

## Releasing

* Make sure `gpg-agent` is running.
* Execute `mvn release:prepare release:perform`

For publishing the site do the following:

```
cd target/checkout
mvn site scm-publish:publish-scm
```

