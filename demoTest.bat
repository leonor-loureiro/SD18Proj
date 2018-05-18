set "handlers=%cd%\ws-handlers"
set "station=%cd%\station-ws"
set "binasWs=%cd%\binas-ws"
set "binasCli=%cd%\binas-ws-cli"
set "stationCli=%cd%\station-ws-cli"

start cmd /c "cd %handlers% & mvn clean install -DskipTests"
timeout /t 5

start cmd /c "mvn  install -DskipTests"
timeout /t 10

start cmd /k "cd %station% & mvn  exec:java"
start cmd /k "cd %station% & mvn exec:java -Dws.i=2"
start cmd /k "cd %station% & mvn exec:java -Dws.i=3"

timeout /t 5
start cmd /k "cd %binasWs% & mvn  exec:java"

timeout /t 5
start cmd /k "cd %binasCli% & mvn exec:java -Dws.showLog=no"

timeout /t 10
start cmd /k "cd %binasCli% & mvn exec:java -Dws.fase=F2 -Dws.showLog=no"