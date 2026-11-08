title: Choosing the elements of the flattened POM

# Choosing the elements of the flattened POM

The parameter `flattenMode` selects a predefined set of POM elements for the flattened POM. The parameter `pomElements` overrides the handling of single elements on top of it, so you can start from the mode that is closest to your needs and adjust it.

## How it works

Each child element of `pomElements` is the name of a POM element, and its text is one of the following values. The flattened POM is written from the Maven model, so the formatting and the blank lines of your POM are not preserved.

|         Value          |                                                                  The element in the flattened POM                                                                  |
|------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `flatten`              | Is reduced to what a consumer needs. For most elements that means it is removed. This is the default for elements that neither the mode nor `pomElements` mention. |
| `expand`               | Comes from the effective POM, so it includes what is inherited from parents and from the Super POM.                                                                |
| `resolve`              | Comes from the resolved POM. Use it for `parent` and `version` to get the CI-friendly variables `revision`, `sha1` and `changelist` replaced.                      |
| `interpolate`          | Comes from your POM with its variables replaced. Nothing is inherited.                                                                                             |
| `extended_interpolate` | Like `interpolate`, but the variables are resolved with the properties of the effective POM.                                                                       |
| `keep`                 | Is copied untouched from your POM, variables included.                                                                                                             |
| `remove`               | Is not written at all.                                                                                                                                             |

The element names are the properties of the POM model, for example `parent`, `name`, `description`, `url`, `scm`, `developers`, `dependencies`, `dependencyManagement`, `pluginManagement`, `properties`, `repositories`, `pluginRepositories` and `build`. The class `FlattenDescriptor` in the Javadoc lists all of them. A handling set in `pomElements` wins over the one that `flattenMode` implies.

Note that `pluginManagement` is a separate element although it lives inside `build`: removing `build` does not remove `pluginManagement`, and the other way round.

## Remove only the dependencies

Keep the elements of an open-source project and drop the dependencies, for example for a shaded artifact:

```xml
<configuration>
  <flattenMode>oss</flattenMode>
  <pomElements>
    <dependencies>remove</dependencies>
  </pomElements>
</configuration>
```

The mode `fatjar` does the same, but keeps fewer elements.

## Resolve CI-friendly variables without the build section

With `flattenMode` set to `resolveCiFriendliesOnly` the variables are replaced everywhere, including in `parent`, but the `build` section stays in the flattened POM. Remove it, together with `pluginManagement`:

```xml
<configuration>
  <flattenMode>resolveCiFriendliesOnly</flattenMode>
  <pomElements>
    <build>remove</build>
    <pluginManagement>remove</pluginManagement>
  </pomElements>
</configuration>
```

## Keep the parent with a resolved version

Except for `resolveCiFriendliesOnly`, the modes remove the `parent` element. If you keep it with `keep`, it still contains the CI-friendly variable. Take the parent from the resolved POM instead, which has the variable replaced:

```xml
<configuration>
  <flattenMode>bom</flattenMode>
  <pomElements>
    <parent>resolve</parent>
  </pomElements>
</configuration>
```

## Keep the plugin management of a parent POM

The mode `bom` keeps `dependencyManagement` and `properties` but removes `pluginManagement`. Add it back when the flattened POM is used as a parent:

```xml
<configuration>
  <flattenMode>bom</flattenMode>
  <pomElements>
    <pluginManagement>interpolate</pluginManagement>
  </pomElements>
</configuration>
```

Maven cannot import `pluginManagement` the way it imports a BOM, so this helps projects that inherit from the flattened POM, not projects that import it.
