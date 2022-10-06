Application execution must be done through command line.
To run application, navigate to directory which contains "Interchanges_v1.0.jar" file and enter command:

java -jar Interchanges_v1.0.jar

By entering full path to the JAR file, application can be executed from any directory:

java -jar full\path\to\file\directory\Interchanges_v1.0.jar

When application is running, follow instructions displayed in the screen.

Testing data is in project "data" folder.
More data can be downloaded from here: http://download.geofabrik.de/
Data must be converted from SHP to CSV format before using in application.
Convertion geometry type must be selected as "WKT".

Examples of application commands:
-I data/lithuania.csv -T r -R 5 -L 900 -O data/lithuania.csv -P j -s >3 -x =4 -c =4 -l =2
-I data/vilnius.csv -T r -R 5 -L 900 -O data/output/vilnius.csv -P rcxj