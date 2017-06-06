# population-origin-calculator

This program reads from MongoDB data and prints three graphs.
The data must inserted into MongoDB before executing this program.

The data can be found at: http://data.ooe.gv.at/files/cms/Mediendateien/OGD/ogd_abtStat/OOE_Bev_Herkunftsland.csv
This data considers the following 5 countries of origin:
Austria, Turkey, Former Yugoslavia, European Union and others.

Requirements:

 •	MongoDB 3.4
  
 •	Java 1.8
 
 •	Maven 3.2.5
 
 Steps (commands) to get the results of the experiment:

1) Build the application: **mvn clean install**
2) Download data from http://data.ooe.gv.at/files/cms/Mediendateien/OGD/ogd_abtStat/OOE_Bev_Herkunftsland.csv
3) Modify downloaded data in order to make it compatible for MongoDB: 
    **Java –jar population-origin-calculator-1.0-SNAPSHOT-jar-with-dependencies.jar –modifyCSV OOE_Bev_Herkunftsland.csv**
4) Start MongoDB: **mongod --dbpath D:\Tools\mongo\data**
5) Insert data to MongoDB: 
    **mongoimport -d population -c origin --type csv --file output.csv –headerline** (output.csv is the output of step 3)
6) If MongoDB is started on localhost port 27017 and the database and the collection is named population and origin, you can use this command:
    **Java –jar population-origin-calculator-1.0-SNAPSHOT-jar-with-dependencies.jar**
    otherwise give this parameters as input:  
    **Java –jar population-origin-calculator-1.0-SNAPSHOT-jar-with-dependencies.jar -calculateCharts localhost 27017 population origin** 
 
Output:

ComparisonSmallestBiggest.jpeg

PercentOfNonAustrian.jpeg

PopulationOriginTotal.jpeg

[![DOI](https://zenodo.org/badge/93341509.svg)](https://zenodo.org/badge/latestdoi/93341509)

https://zenodo.org/badge/latestdoi/93341509
