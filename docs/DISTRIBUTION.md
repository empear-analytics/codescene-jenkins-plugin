# Distributing a Plugin
To create a distribution image of your plugin, run the following Maven command:

```
mvn package
```
This should create target/*.hpi file. Other users can use Jenkins' web UI to upload this plugin to Jenkins (or place it in $JENKINS_HOME/plugins.)

## Releasing a Plugin and Hosting a Plugin on jenkins-ci.org
https://wiki.jenkins-ci.org/display/JENKINS/Hosting+Plugins

## Using custom builds of plugins included in the Jenkins WAR
If you are building a patched version of one of the plugins in the Jenkins core, the deployment process is a bit different. This is because Jenkins will itself manage these plugins unless you tell it not to.

## Deploying a custom build of a core plugin
1. Stop Jenkins
2. Copy the custom HPI file to $JENKINS_HOME/plugins
3. Remove the previously expanded plugin directory
4. Create an empty file called <plugin>.hpi.pinned - e.g. maven-plugin.hpi.pinned
5. Start Jenkins
