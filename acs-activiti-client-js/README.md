# Adding the acs-http-js-extension project's JAR file to your local maven repository.

The activiti client jar uses the HTTPRequest object in the acs-http-js-1.0.0.jar to avoid trying to share raw source code between two different projects

Based on [this Stack Overflow answer](https://stackoverflow.com/questions/4955635/how-to-add-local-jar-files-to-a-maven-project) the command to use is

```bash
mvn install:install-file \
   -Dfile=<path-to-file> \
   -DgroupId=<group-id> \
   -DartifactId=<artifact-id> \
   -Dversion=<version> \
   -Dpackaging=<packaging> \
   -DgeneratePom=true
 ```
 
 The entry in the project pom.xml is
 
 ```xml
	<dependency>
		<groupId>com.alfresco.se</groupId>
		<artifactId>acs-http-js</artifactId>
		<version>1.0.0</version>
	</dependency>
  ```
  
so it follows as

```bash
mvn install:install-file -Dfile=acs-http-js-1.0.0.jar -DgroupId=com.alfresco.se -DartifactId=acs-http-js \
  -Dversion=1.0.0 -Dpackaging=jar -DgeneratPom=true
```

# Alfresco ACS JAR Module - SDK 4.6

This is an ACS project for Alfresco SDK 4.6.

Run with `./run.sh build_start` or `./run.bat build_start` and verify that it

 * Runs Alfresco Content Service (ACS)
 * (Optional) Runs Alfresco Share
 * Runs Alfresco Search Service (ASS)
 * Runs PostgreSQL database
 * Deploys the JAR assembled module
 
All the services of the project are now run as docker containers. The run script offers the next tasks:

 * `build_start`. Build the whole project, recreate the ACS docker image, start the dockerised environment composed by ACS, Share (optional), ASS 
 and PostgreSQL and tail the logs of all the containers.
 * `build_start_it_supported`. Build the whole project including dependencies required for IT execution, recreate the ACS docker image, start the dockerised environment 
 composed by ACS, Share (optional), ASS and PostgreSQL and tail the logs of all the containers.
 * `start`. Start the dockerised environment without building the project and tail the logs of all the containers.
 * `stop`. Stop the dockerised environment.
 * `purge`. Stop the dockerised container and delete all the persistent data (docker volumes).
 * `tail`. Tail the logs of all the containers.
 * `reload_acs`. Build the ACS module, recreate the ACS docker image and restart the ACS container.
 * `build_test`. Build the whole project, recreate the ACS docker image, start the dockerised environment, execute the integration tests and stop 
 the environment.
 * `test`. Execute the integration tests (the environment must be already started).

# Few things to notice

 * No parent pom
 * No WAR projects, the jars are included in the custom docker images
 * No runner project - the Alfresco environment is now managed through [Docker](https://www.docker.com/)
 * Standard JAR packaging and layout
 * Works seamlessly with Eclipse and IntelliJ IDEA
 * JRebel for hot reloading, JRebel maven plugin for generating rebel.xml [JRebel integration documentation]
 * AMP as an assembly
 * Persistent test data through restart thanks to the use of Docker volumes for ACS, ASS and database data
 * Resources loaded from META-INF
 * Web Fragment (this includes a sample servlet configured via web fragment)

# TODO

  * Abstract assembly into a dependency so we don't have to ship the assembly in the archetype
  * Functional/remote unit tests
