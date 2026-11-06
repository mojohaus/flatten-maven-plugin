title: Flatten Maven Plugin

# Flatten Maven Plugin

The Flatten Maven Plugin generates a flattened version of the pom.xml that Maven installs and deploys instead of the original. The flattened POM has the following characteristics:

- Build specific elements are removed
- Development specific elements are removed by default
- It only contains elements required for users of your artifact
- Its variables are resolved
- Its parent relationship is resolved, flattened and removed
- Its build time driven profiles **can** be evaluated so their impact gets embedded
- JDK or OS driven profiles still remain allowing dynamic dependencies if needed

For a complete specification of how the flattened POM is built see [here](flatten-mojo.html). This allows the following possibilities and features:

- You can keep the versions of parent POMs fixed and use them for development only distributed via version control only instead of maven repositories. This way you can use centralized variables and dependencyManagement without maintaining and releasing parent POM versions.
- You can use a lot of the flexibility of maven but avoid various mistakes resulting in installing or deploying inconsistent POMs.

## Goals

- [flatten:flatten](flatten-mojo.html) generates the flattened POM and sets it as file of the maven project.
- [flatten:clean](clean-mojo.html) removes any files created by `flatten:flatten`.

## Usage

General instructions on how to use the Flatten Maven Plugin can be found on the [usage page](usage.html).

If you have questions regarding the plugin's usage, contact the [user mailing list](mailing-lists.html). Posts to the mailing list are archived and could already contain the answer to your question as part of an older thread. Hence, it is also worth browsing/searching the mail archive.

You can fill a feature request or bug report in our [issue tracker](issue-management.html). When creating a new issue, please provide a comprehensive description of your concern. Especially for fixing bugs it is crucial that the developers can reproduce your problem. Entire debug logs, POMs and most preferably little demo projects attached to the issue are very much appreciated. Of course, patches are welcome too. Contributors can check out the project from our [source repository](scm.html) and will find supplementary information in the [guide to helping with Maven](https://maven.apache.org/guides/development/guide-helping.html).
