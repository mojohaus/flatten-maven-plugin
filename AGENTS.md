# Agent guide for flatten-maven-plugin

This file is for automated agents and human contributors working on this repository. It records
what the code and the POMs do not make obvious.

## Layout

- One module with two goals: `flatten` (`FlattenMojo`) and `clean` (`CleanMojo`), in
  `org.codehaus.mojo.flatten`. The flatten modes and the per-element handling are documented in
  the Javadoc of `FlattenMojo`, `FlattenMode` and `ElementHandling`, which the plugin site renders.
- `model/resolution` resolves parent and imported POMs from the reactor and the repositories.
  `cifriendly` interpolates only the CI-friendly version properties, for the
  `resolveCiFriendliesOnly` mode. `extendedinterpolation` backs the `interpolate` and
  `extended_interpolate` element handling.
- The site is built from the plain Markdown pages in `src/site/markdown`, such as `usage.md` and
  the FAQ `faq.md`.

## Build gates

- Main code must use the Java 8 API: mojo-parent sets `maven.compiler.release` to 8. There is no
  animal-sniffer or Checkstyle execution, so building on JDK 8 is the only other check. CI builds
  on the oldest and the newest JDKs through the shared MojoHaus build workflow.
- Spotless runs on JDK 17 and later only. It applies the formatting in `process-sources` on a local
  build and checks it when the `CI` environment variable is set. It formats Java with
  palantir-java-format, sorts the POM and also reformats Markdown, so run `mvn spotless:apply`
  after editing the README or this file.
- The plugin compiles against the Maven 3.9.x release in `mavenVersion` and declares a literal
  `<prerequisites><maven>3.9.0</maven>`; keep the two apart, so raising `mavenVersion` does not
  raise the prerequisite. Building the plugin itself needs the Maven version in
  `minimalMavenBuildVersion`. The shared workflow runs the build and the integration tests on
  Maven 3 and on Maven 4.

## Unit tests

- Fixture projects for unit tests live in `src/test/resources/<case>/pom.xml`. Tests that need
  dependency resolution (`nested-exclusion`, `conflict-winner`) carry their own local repository in
  the fixture's `repository` directory; keep new ones offline the same way.
- Prefer a unit test. Add an integration test only for what needs a real Maven build: reactors,
  parent POMs and imported BOMs resolved from a repository, profile activation by the running JDK
  or OS, or a goal bound into the lifecycle (`install`, `deploy`).

## Integration tests

- They live in `src/it/projects`, one directory per test. Unless `invoker.properties` sets
  `invoker.goals`, each runs `flatten -Drevision=1.2.3.4`. `src/it/mrm/settings.xml` mirrors every
  repository to a mock repository manager that serves `src/it/mrm/repository`, the IT local
  repository, and a proxy to Maven Central. Some tests resolve real POMs through that proxy, such
  as the Spring Boot BOM imported by `bom-flattenMode` and expanded by `bom-pomElements`.
- Run a subset:

  ```shell
  mvn -P run-its verify -Dinvoker.test=bom-flattenMode
  ```

  `-Dinvoker.test` filters the setup project too. `custom-packaging` needs
  `setup-customarchive-maven-plugin`, so list both:
  `-Dinvoker.test=custom-packaging,setup-customarchive-maven-plugin`.

- They run in parallel, one per CPU core (`invoker.parallelThreads` 1C), so a test must
  not depend on another test's output.

- The forked builds get `JAVA_TOOL_OPTIONS=-XX:TieredStopAtLevel=1 -XX:+UseSerialGC`, so every JVM
  prints a `Picked up JAVA_TOOL_OPTIONS` line on stderr.

- Write `verify.groovy` as `assert` statements. Invoker fails a script whose last expression is
  anything other than `true` or `null`, so a trailing `list.each { }` fails the test; use a `for`
  loop.

- Restrict a test to some Maven versions with `invoker.maven.version` in `invoker.properties`, as
  `no-overwrite-3.6.3-later` and `no-overwrite-4.0.0-later` do.

## Fixtures and dependencies

- POMs under `src/it` and `src/test/resources` pin old versions on purpose. Do not update them.
  Dependabot excludes both paths from version updates, but security updates can still propose
  them; close those pull requests. Coordinates that a test only copies and never resolves can use
  the `localhost` group ID, which keeps them out of security alerts.
- Deliberate pins in `.github/dependabot.yml`: plexus-xml stays below 4 because 4.x is the Maven 4
  line, and slf4j below 2 because Maven 3.9 exports slf4j-api 1.7.36.

## Documentation updates

A change that users or contributors can notice ships with the documentation it affects, in the
same pull request. A refactor with no visible effect needs none.

- A new or changed parameter, flatten mode or element handling: the Javadoc of `FlattenMojo`,
  `FlattenMode` or `ElementHandling`, which becomes the goal page, and the goal list in
  `src/site/markdown/index.md`.
- New or changed usage: `src/site/markdown/usage.md`, or an example under
  `src/site/markdown/examples`. The site pages are plain Markdown; the
  first plugin declaration in `usage.md` writes the version as
  `<version><!-- latest release --></version>` and the examples leave it out, so they never go
  stale. Keep the flattened POM shown in an example equal to what the plugin
  writes.
- Recurring questions: `src/site/markdown/faq.md`.
- A page added, removed or renamed: the menu in `src/site/site.xml`. Never rename a published page;
  its URL is linked from elsewhere.
- The Maven or Java requirement, or how to build and release: `README.md`.
- A new convention, build gate or test fixture: this file.

Check the result with `mvn site` and open `target/site/index.html`.

## Pull requests

- Pull requests are squash- or rebase-merged; merge commits are disabled. Release Drafter builds
  the release notes from the titles and labels, so the title should describe the change for a
  user.

