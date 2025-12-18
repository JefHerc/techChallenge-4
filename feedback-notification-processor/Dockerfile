## Estágio 1: Build Nativo com Java 21 e Mandrel
FROM quay.io/quarkus/ubi-quarkus-mandrel-builder-image:jdk-21 AS build

COPY --chown=quarkus:quarkus mvnw /code/mvnw
COPY --chown=quarkus:quarkus .mvn /code/.mvn
COPY --chown=quarkus:quarkus pom.xml /code/

USER quarkus
WORKDIR /code

# Dá permissão de execução ao wrapper (importante para Linux/Docker)
RUN chmod +x mvnw && \
    touch src/main/resources/application.properties # Cria arquivo dummy se não existir para evitar erro no copy

# Baixa as dependências usando o wrapper
RUN ./mvnw -B org.apache.maven.plugins:maven-dependency-plugin:3.1.2:go-offline

COPY src /code/src

# Compila usando o wrapper
RUN ./mvnw package -Pnative -Dquarkus.native.native-image-xmx=4g

## Estágio 2: Criação da Imagem Lambda (Runtime)
FROM public.ecr.aws/lambda/provided:al2023

COPY --from=build /code/target/*-runner /var/runtime/bootstrap

CMD ["bootstrap"]