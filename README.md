The application the challenge proposed by wifiesta. It has been build using spring-boot, H2 and integrate Spock on maven.

**Test**: After running "mvn clean test" you can see the test report under build/spock-report. 95% of code has been covered

**Run**: you can run the following commands
"mvn clean package"
"java -jar -Dspring.profiles.active=development target/menus-0.0.1-SNAPSHOT.jar"

Another method to run the app
"mvn spring-boot:run -Dspring-boot.run.profiles=development"

(development is the profile that handle mock entities)
