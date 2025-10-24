# Build stage (usa Maven com JDK 21; não precisa do mvnw)
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
# contexto do build deve ser a RAIZ do repo (compose aponta: context: .)
COPY . .
RUN mvn -q -T 1C -pl service-b -am clean package -DskipTests

# Runtime stage (JRE 21 enxuto)
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/service-b/target/service-b-*.jar /app/app.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","/app/app.jar"]
