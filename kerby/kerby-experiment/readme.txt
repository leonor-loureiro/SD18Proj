This is a simple Java application to experiment with Kerberos.

Configuration values are loaded from a property file in resources.

The properties file follows the name=value syntax,
explained in http://en.wikipedia.org/wiki/.properties

The min and max values are simple properties.
The workDir property value is set using a Maven defined property,
using the ${work.dir} syntax.


Instructions using Maven:
-----------------------

To compile and copy resources to the output directory:
  mvn package
  (compare the properties file in src/main with
  the file in target/classes/ after executing)

To run using exec plugin:
    mvn exec:java

To generate launch scripts for Windows and Linux:
  (appassembler:assemble is attached to install phase)
  mvn install

To run using appassembler plugin:
  On Windows:
    target\appassembler\bin\java-app_config
  On Linux:
    ./target/appassembler/bin/java-app_config


To configure the Maven project in Eclipse:
-----------------------------------------

'File', 'Import...', 'Maven'-'Existing Maven Projects'
'Select root directory' and 'Browse' to the project base folder.
Check that the desired POM is selected and 'Finish'.


--
Revision date: 2018-05-03
leic-sod@disciplinas.tecnico.ulisboa.pt
