# MojoHaus Flatten Maven Plugin

This is the [flatten-maven-plugin](http://www.mojohaus.org/flatten-maven-plugin/).
 
[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/mojohaus/versions-maven-plugin.svg?label=License)](http://www.apache.org/licenses/)
[![Maven Central](https://img.shields.io/maven-central/v/org.codehaus.mojo/flatten-maven-plugin.svg?label=Maven%20Central)](https://search.maven.org/artifact/org.codehaus.mojo/flatten-maven-plugin)
[![GitHub CI](https://github.com/mojohaus/flatten-maven-plugin/actions/workflows/maven.yml/badge.svg)](https://github.com/mojohaus/flatten-maven-plugin/actions/workflows/maven.yml)
1.0.x branch: [![GitHub CI](https://github.com/mojohaus/flatten-maven-plugin/actions/workflows/maven.yml/badge.svg?branch=1.0.x)](https://github.com/mojohaus/flatten-maven-plugin/actions/workflows/maven.yml)

## Quickstart
This plugin generates a flattened version of your pom.xml and makes maven to install and deploy this one instead of the original pom.xml.
```
  <build>
    <plugins>
      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>flatten-maven-plugin</artifactId>
        <!--<version>INSERT LATEST VERSION HERE</version>-->
        <executions>
          <execution>
            <goals>
              <goal>flatten</goal>
            </goals>
          </execution>
        </executions>
        <configuration>
          <!-- See usage on maven site from link above for details -->
        </configuration>
      </plugin>
    </plugins>
  </build>
```

## Releasing

* Make sure `gpg-agent` is running.
* Execute `mvn -B release:prepare release:perform`

For publishing the site do the following:

```
cd target/checkout
mvn verify site site:stage scm-publish:publish-scm
```
