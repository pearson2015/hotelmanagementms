FROM openjdk:17
COPY ./target/hotelmanagementms.jar /hotelmanagementms.jar
CMD ["java", "-jar", "/hotelmanagementms.jar"]