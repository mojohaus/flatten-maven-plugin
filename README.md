# MojoHaus Flatten Maven Plugin

This is the [flatten-maven-plugin](http://www.mojohaus.org/flatten-maven-plugin/).
 
[![Build Status](https://travis-ci.org/mojohaus/flatten-maven-plugin.svg?branch=master)](https://travis-ci.org/mojohaus/flatten-maven-plugin)

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
              <goal>generate</goal>
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
