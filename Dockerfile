FROM maven:3.6.3-jdk-11
COPY ./ ./
RUN mvn clean package -DskipTests
CMD ["java", "-jar", "target/api-gateway-1.0.0-SNAPSHOT-fat.jar","-conf ./conf/application.json"]