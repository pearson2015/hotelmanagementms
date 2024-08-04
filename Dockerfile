FROM openjdk:21
COPY ./target/hotelmanagementms-0.0.1-SNAPSHOT.jar /hotelmanagementms.jar
CMD ["java", "-jar", "/hotelmanagementms.jar"]