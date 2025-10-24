# Stage de build com Maven + JDK 21
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
# copia o projeto inteiro (precisa do contexto na raiz!)
COPY . .
# builda só o módulo service-a com dependências ascendentes
RUN mvn -q -T 1C -pl service-a -am clean package -DskipTests

# Stage final com JRE
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/service-a/target/service-a-*.jar /app/app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]
