FROM docker.io/eclipse-temurin:8-jdk

MAINTAINER Christoph Kappel <christoph@unexist.dev>

RUN apt-get -o Acquire::Max-FutureTime=31536000 update -y \
    && export DEBIAN_FRONTEND=noninteractive \
    && apt-get install -y --no-install-recommends curl \
    && apt-get clean

RUN groupadd wheel \
    && useradd -m hduser -G wheel \
    && echo "hduser:hduser" | chpasswd

ENV SPARK_USER="hduser"
ENV SPARK_VERSION="3.5.1"
ENV SPARK_HOME="/home/${SPARK_USER}/spark-${SPARK_VERSION}-bin-hadoop3"
ENV PATH="${PATH}:${HADOOP_HOME}/bin:${HADOOP_HOME}/sbin"

RUN curl -sL --retry 3  https://archive.apache.org/dist/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop3.tgz \
    | tar xz -C /home/${SPARK_USER}/ \
    && rm -rf ${SPARK_HOME}/examples

COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh

WORKDIR /home/hduser
USER hduser

EXPOSE 7077 8080 8081

# WORKERSTART=0 will prevent worker from being launched
ENV WORKERSTART=1

ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]