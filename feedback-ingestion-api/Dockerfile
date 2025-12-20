## Estágio 1: Build Nativo com Java 21 e Mandrel
FROM quay.io/quarkus/ubi-quarkus-mandrel-builder-image:jdk-21 AS build

COPY --chown=quarkus:quarkus mvnw /code/mvnw
COPY --chown=quarkus:quarkus .mvn /code/.mvn
COPY --chown=quarkus:quarkus pom.xml /code/

USER quarkus
WORKDIR /code

# --- CORREÇÃO ADICIONADA AQUI ---
# Remove caracteres do Windows (\r) do script mvnw para evitar erro de bad interpreter
RUN sed -i 's/\r$//' mvnw
# --------------------------------

# Dá permissão de execução
RUN chmod +x mvnw

# Baixa as dependências
RUN ./mvnw -B org.apache.maven.plugins:maven-dependency-plugin:3.1.2:go-offline

COPY src /code/src

# Compila
RUN ./mvnw package -Pnative -Dquarkus.native.native-image-xmx=4g

## Estágio 2: Criação da Imagem Lambda (Runtime)
FROM public.ecr.aws/lambda/provided:al2023

COPY --from=build /code/target/*-runner /var/runtime/bootstrap

CMD ["bootstrap"]