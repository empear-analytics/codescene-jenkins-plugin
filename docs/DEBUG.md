
# Debugging the Plugin

If you are using Netbeans you can just hit debug for all others
```
mvnDebug hpi:run
```

#### Linux, Unix, OSX
```
export MAVEN_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8000,suspend=n"
mvn hpi:run
```

#### Windows
```
set MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8000,suspend=n
mvn hpi:run
```

If you open http://localhost:8080/jenkins in your browser, you should see the Jenkins page running in Jetty. The MAVEN_OPTS portion launches this whole thing with the debugger port 8000, so you should be able to start a debug session to this port from your IDE.

Once this starts running, keep it running. Jetty will pick up all the changes automatically.

When you make changes to view files in src/main/resources or resource files in src/main/webapp, just hit F5 in your browser to see the changes.
When you change Java source files, compile them in your IDE and Jetty should automatically redeploy Jenkins to pick up those changes. There is no need to run mvn at all.

## Changing port
If you need to launch the Jenkins on a different port than 8080, set the port through the system property jetty.port.
```
mvn hpi:run -Djetty.port=8090
```