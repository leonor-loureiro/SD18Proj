# Projeto de Sistemas Distribuídos 2017/18 #

Grupo T08

André Fonseca	84698	andre.filipe.alves.fonseca@ist.utl.pt

Diogo Andrade	84709	diogo.freire.dandrade@gmail.com

Leonor Loureiro	84736	leonor.filipa@tecnico.ulisboa.pt

------------------------------------------------------------------------------
##Instructions for testing:##

###### on project root run install project with:
	T08-SD18Proj$		mvn install -DskipTests 

###### open 5 terminals proceed to each folder and insert commands folder
	[1]	[T08-SD18Proj/station-ws]$ 		mvn exec:java  
	[2] [T08-SD18Proj/station-ws]$ 		mvn exec:java -Dws.i=2
	[3] [T08-SD18Proj/station-ws]$ 		mvn exec:java -Dws.i=3
	[4] [T08-SD18Proj/binas-ws]$   		mvn exec:java
	[5] [T08-SD18Proj/binas-ws-cli]$	mvn verify
	
	
-------------------------------------------------------------------------------
**FIM**
