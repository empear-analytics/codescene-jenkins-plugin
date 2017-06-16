## Built current version of plugin and install it into JENKINS_HOME/plugins directory.
## Restarts jenkins via API.
set -e
set -x

JENKINS_URL="http://localhost:8080"
JENKINS_HOME="/Users/jumar/.jenkins"
USER_PASSWORD="admin:admin"

# build & deploy plugin
mvn package -DskipTests=true
cp target/codescene.hpi "$JENKINS_HOME/plugins/"

## restart jenkins

# we need crumb (CSRF token) for proper authentication
CRUMB=$(curl --user "$USER_PASSWORD" -s $JENKINS_URL'/crumbIssuer/api/xml?xpath=concat(//crumbRequestField,":",//crumb)')

curl --user "$USER_PASSWORD" -X POST -H "$CRUMB" "$JENKINS_URL/restart"

