FROM eclipse-temurin:11-jdk as builder

MAINTAINER Christoph Kappel <christoph@unexist.dev>

RUN apt-get -qq update -o Acquire::Max-FutureTime=604800 \
    && apt-get -qq install --no-install-recommends maven unzip less \
    && apt-get clean

COPY . /app
WORKDIR /app

RUN mvn clean package -Passembly -Dmaven.test.skip \
    && unzip /app/debezium-server-iceberg-dist/target/debezium-server-iceberg-dist*.zip -d appdist

FROM eclipse-temurin:11-jre

COPY --from=builder /app/appdist/debezium-server-iceberg/ /app/
COPY --from=builder /app/application.properties /app/conf/application.properties

WORKDIR /app

EXPOSE 8080 8083

#VOLUME ["/app/conf", "/app/data"]

ENTRYPOINT ["/app/run.sh"]