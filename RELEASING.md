# Releasing the CodeScene Jenkins Plugin

## Changelog

First, make sure [the CHANGELOG](README.md#changelog) is updated with the
latest features and changes.

## Version Tagging

To create a release, first run:

``` bash
mvn release:prepare -Dresume=false
```

This will prompt you for a release version label, e.g. `1.2.3`. Then it prompts
you for the git tag, which in this scenario should be `v1.2.3`. Finally, you
should set the coming SNAPSHOT version to, in this scenario, `1.2.4-SNAPSHOT`.

The release plugin will create a tag in git and push the changes to GitHub,
triggering a Travis build.

Do **NOT** run `mvn release:perform`, just sit back and watch the Travis build
pass, and your tagged release should eventually appear as a [GitHub
Release](https://github.com/empear-analytics/codescene-jenkins-plugin/releases).
The `.hpi` file is automatically added to the release, available for users to
download and then upload in Jenkins.
