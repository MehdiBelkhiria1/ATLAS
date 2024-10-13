## A lightweight Java API for Variants discovery from HTS data.

### Building Atlas
in the root directory:
 - compile and build a jar 
 ```
 mvn clean install
 ```
The jar will be found under \ATLAS\target\ATLAS-\<version\>.jar

- run tests
 ```
mvn test
 ```
-dockerizing the project
 ```
docker build -t ATLAS-api .
 ```
### How to  use
 - with cmd example 
 ```
java -jar C:\\ATLAS-1.0.0-SNAPSHOT.jar view -f C:\\reference.fa -i C:\\bamFile.bam -o C:\\results.txt -r chrY:0-61911150
 ```
- with Docker example
 ```
docker run --rm -v C:\\reference.fa:/data/reference.0.fa -v C:\\bamFile.bam:/data/bamFile.bam  ATLAS-api  view -f /data/reference.fa -i /data/bamFile.bam -o /data/results.txt -r chrY:0-61911150
 ```
