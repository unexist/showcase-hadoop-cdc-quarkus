FROM eclipse-temurin:8-jdk-alpine

RUN apk add curl openssh bash doas

RUN adduser hduser -G wheel; \
    echo "hduser:hduser" | chpasswd; \
    echo "permit nopass :wheel" > /etc/doas.d/doas.conf

COPY ssh_config /etc/ssh/ssh_config
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh

WORKDIR /home/hduser
USER hduser
RUN ssh-keygen -t rsa -P "" -f ~/.ssh/id_rsa \
    && cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys \
    && chmod 0600 ~/.ssh/authorized_keys

ENV HADOOP_USER="hduser"
ENV HADOOP_VERSION="3.3.4"
ENV HADOOP_HOME="/home/${HADOOP_USER}/hadoop-${HADOOP_VERSION}"

RUN curl -sL --retry 3 "http://archive.apache.org/dist/hadoop/common/hadoop-${HADOOP_VERSION}/hadoop-${HADOOP_VERSION}.tar.gz" \
  | tar -xz -C /home/${HADOOP_USER}/ \
  && rm -rf ${HADOOP_HOME}/share/doc

ENV HDFS_NAMENODE_USER="${HADOOP_USER}"
ENV HDFS_DATANODE_USER="${HADOOP_USER}"
ENV HDFS_SECONDARYNAMENODE_USER="${HADOOP_USER}"
ENV YARN_RESOURCEMANAGER_USER="${HADOOP_USER}"
ENV YARN_NODEMANAGER_USER="${HADOOP_USER}"

RUN echo "export JAVA_HOME=/opt/java/openjdk/" >> ${HADOOP_HOME}/etc/hadoop/hadoop-env.sh
RUN echo "export HADOOP_OPTS='-Ddfs.namenode.name.dir=file://${HADOOP_HOME}/hdfs/name -Ddfs.datanode.name.dir=file://${HADOOP_HOME}/hdfs/data'" >> ${HADOOP_HOME}/etc/hadoop/hadoop-env.sh

COPY core-site.xml ${HADOOP_HOME}/etc/hadoop/
COPY hdfs-site.xml ${HADOOP_HOME}/etc/hadoop/
COPY yarn-site.xml ${HADOOP_HOME}/etc/hadoop/

ENV PATH="${PATH}:${HADOOP_HOME}/bin:${HADOOP_HOME}/sbin"

EXPOSE 50070 50075 50010 50020 50090 8020 9000 9864 9870 10020 19888 8088 8030 8031 8032 8033 8040 8042 22

# YARNSTART=0 will prevent yarn scheduler from being launched
ENV YARNSTART=0

# FORMATNODES=0 will prevent formatter
ENV FORMATNODES=1

ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]