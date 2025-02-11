#FROM arm64v8/openjdk:8-jdk-slim
FROM openjdk:8-jre
WORKDIR /AIO
ADD ./lib /AIO/lib
ADD AIO-company-0.0.1-SNAPSHOT.jar /AIO
CMD java -jar AIO-company-0.0.1-SNAPSHOT.jar
EXPOSE 8089