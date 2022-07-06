# syntax=docker/dockerfile:1
FROM openjdk:16-alpine3.13
WORKDIR /usbddx
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
#COPY ../usbddc/pom.xml ./usbddc/
COPY usbdda/pom.xml ./usbdda/
RUN ./mvnw dependency:go-offline
COPY src ./src
#COPY ../usbddc/src ./usbddc/src
COPY usbdda/src ./usbdda/src
CMD ["./mvnw", "spring-boot:run"]