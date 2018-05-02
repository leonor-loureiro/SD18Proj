set "station=%cd%\station-ws"
set "binasWs=%cd%\binas-ws"
set "binasCli=%cd%\binas-ws-cli"
set "stationCli=%cd%\station-ws-cli"


start cmd /k "cd %station% & mvn clean install exec:java"
timeout /t 15
start cmd /k "cd %station% & mvn exec:java -Dws.i=2"
start cmd /k "cd %station% & mvn exec:java -Dws.i=3"
timeout /t 15
start cmd /c "cd %stationCli% & mvn clean install"
timeout /t 15
start cmd /c "cd %binasWs% & mvn clean install exec:java"
timeout /t 15
start cmd /k "cd %binasCli% & mvn clean install"