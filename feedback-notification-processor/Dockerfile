## Estágio 1: Build Nativo com Java 21 e Mandrel
FROM quay.io/quarkus/ubi-quarkus-mandrel-builder-image:jdk-21 AS build

COPY --chown=quarkus:quarkus mvnw /code/mvnw
COPY --chown=quarkus:quarkus .mvn /code/.mvn
COPY --chown=quarkus:quarkus pom.xml /code/

USER quarkus
WORKDIR /code

# CORREÇÃO AQUI: Criamos a pasta antes de criar o arquivo
RUN chmod +x mvnw && \
    mkdir -p src/main/resources && \
    touch src/main/resources/application.properties

# Baixa as dependências (Cache Layer)
RUN ./mvnw -B org.apache.maven.plugins:maven-dependency-plugin:3.1.2:go-offline

# Copia o código fonte real (Isso vai sobrescrever o dummy criado acima, o que é ótimo)
COPY src /code/src

# Compila
RUN ./mvnw package -Pnative -Dquarkus.native.native-image-xmx=4g

## Estágio 2: Criação da Imagem Lambda (Runtime)
FROM public.ecr.aws/lambda/provided:al2023

COPY --from=build /code/target/*-runner /var/runtime/bootstrap

CMD ["bootstrap"]