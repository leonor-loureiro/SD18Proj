# kerby
Simplified implementation of the Kerberos V5 protocol

The simplified version of the Kerberos has only the Saut component.
The TGT is not present.


## Build

To compile and install all modules:
```
mvn clean install -DskipTests
```
The tests are skipped because they require the server to be running.

To generate a combined javadoc:
```
mvn javadoc:aggregate -pl :kerby,:kerby-lib,:kerby-ws-cli
```
The javadoc aggregates classes from the mentioned modules.
