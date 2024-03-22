FROM docker.io/eclipse-temurin:8-jdk

MAINTAINER Christoph Kappel <christoph@unexist.dev>

RUN apt-get -o Acquire::Max-FutureTime=31536000 update -y \
    && export DEBIAN_FRONTEND=noninteractive \
    && apt-get install -y --no-install-recommends doas curl ssh \
    && apt-get clean

RUN groupadd wheel \
    && useradd -m hduser -G wheel \
    && echo "hduser:hduser" | chpasswd \
    && echo "permit nopass :wheel" > /etc/doas.conf

COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
COPY ssh_config /etc/ssh/ssh_config

WORKDIR /home/hduser
USER hduser

RUN ssh-keygen -t rsa -P "" -f ~/.ssh/id_rsa \
    && cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys \
    && chmod 0600 ~/.ssh/authorized_keys

ENV HADOOP_USER="hduser"
ENV HADOOP_VERSION="3.3.4"
ENV HADOOP_HOME="/home/${HADOOP_USER}/hadoop-${HADOOP_VERSION}"
ENV PATH="${PATH}:${HADOOP_HOME}/bin:${HADOOP_HOME}/sbin"

RUN curl -sL --retry 3 "http://archive.apache.org/dist/hadoop/common/hadoop-${HADOOP_VERSION}/hadoop-${HADOOP_VERSION}.tar.gz" \
    | tar -xz -C /home/${HADOOP_USER}/ \
    && rm -rf ${HADOOP_HOME}/share/doc

ENV HDFS_NAMENODE_USER="${HADOOP_USER}"
ENV HDFS_DATANODE_USER="${HADOOP_USER}"
ENV HDFS_SECONDARYNAMENODE_USER="${HADOOP_USER}"
ENV YARN_RESOURCEMANAGER_USER="${HADOOP_USER}"
ENV YARN_NODEMANAGER_USER="${HADOOP_USER}"

RUN echo "export JAVA_HOME=/opt/java/openjdk/" >> ${HADOOP_HOME}/etc/hadoop/hadoop-env.sh \
    && echo "export HADOOP_OPTS='-Ddfs.namenode.name.dir=file://${HADOOP_HOME}/hdfs/namenode -Ddfs.datanode.name.dir=file://${HADOOP_HOME}/hdfs/datanode'" >> ${HADOOP_HOME}/etc/hadoop/hadoop-env.sh

# Create paths
RUN mkdir -p ${HADOOP_HOME}/hdfs/namenode \
    && mkdir -p ${HADOOP_HOME}/hdfs/datanode \
    && mkdir -p ${HADOOP_HOME}/logs \
    && chmod -R 755 ${HADOOP_HOME}/hdfs/

COPY core-site.xml ${HADOOP_HOME}/etc/hadoop/
COPY hdfs-site.xml ${HADOOP_HOME}/etc/hadoop/
COPY yarn-site.xml ${HADOOP_HOME}/etc/hadoop/

EXPOSE 50070 50075 50010 50020 50090 8020 9000 9864 9870 10020 19888 8088 8030 8031 8032 8033 8040 8042 22

# FORMATNODES=0 will prevent formatter
ENV FORMATNODES=1

# YARNSTART=0 will prevent yarn scheduler from being launched
ENV YARNSTART=0

# MAPREDUCESTART=0 will prevent mapreduce from being launched
ENV MAPREDUCESTART=0

ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]