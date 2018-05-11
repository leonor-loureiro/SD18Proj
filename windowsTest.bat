set "handlers=%cd%\ws-handlers"
set "station=%cd%\station-ws"
set "binasWs=%cd%\binas-ws"
set "binasCli=%cd%\binas-ws-cli"
set "stationCli=%cd%\station-ws-cli"

rem  -- installs, tests and sets running station-ws (stations)
start cmd /c "cd %handlers% & mvn clean install"
start cmd /k "title station1 & cd %station% & mvn clean install exec:java"
timeout /t 15

rem -- runs 2 more stations for next tests/installs (stations)
start cmd /k "title station2 & cd %station% & mvn exec:java -Dws.i=2"
start cmd /k "title station3 & cd %station% & mvn exec:java -Dws.i=3"
timeout /t 15

rem -- installs and test station-ws-cli (station clients)
start cmd /c "cd %stationCli% & mvn clean install"
timeout /t 15

rem -- install, tests and sets binas-ws running (Binas)
start cmd /c "title binas-ws & cd %binasWs% & mvn clean install exec:java"
timeout /t 15

rem -- install and run tests for binas-ws-cli (Binas client)
start cmd /k "title binas-ws-cli & cd %binasCli% & mvn clean install"