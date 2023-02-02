FROM eclipse-temurin:8-jdk

MAINTAINER Christoph Kappel <christoph@unexist.dev>

RUN apt-get -o Acquire::Max-FutureTime=604800 update -y \
    && export DEBIAN_FRONTEND=noninteractive \
    && apt-get install -y --no-install-recommends doas curl ssh less \
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

ENV HIVE_VERSION="3.1.3"
ENV HIVE_HOME="/home/${HADOOP_USER}/hive-${HIVE_VERSION}"

ENV PATH="${PATH}:${HADOOP_HOME}/bin:${HADOOP_HOME}/sbin:${HIVE_HOME}/bin"

# Download and install Hadoop
RUN curl -sL --retry 3 "http://archive.apache.org/dist/hadoop/common/hadoop-${HADOOP_VERSION}/hadoop-${HADOOP_VERSION}.tar.gz" \
    | tar -xz -C /home/${HADOOP_USER}/ \
    && rm -rf ${HADOOP_HOME}/share/doc

COPY core-site.xml ${HADOOP_HOME}/etc/hadoop/
COPY hdfs-site.xml ${HADOOP_HOME}/etc/hadoop/
COPY yarn-site.xml ${HADOOP_HOME}/etc/hadoop/
COPY mapred-site.xml ${HADOOP_HOME}/etc/hadoop/

# Set options based on variables
RUN echo "export JAVA_HOME=\"/opt/java/openjdk/\"" >> ${HADOOP_HOME}/etc/hadoop/hadoop-env.sh \
    && echo "export HDFS_NAMENODE_USER=\"${HADOOP_USER}\"" >> ${HADOOP_HOME}/etc/hadoop/hadoop-env.sh \
    && echo "export HDFS_DATANODE_USER=\"${HADOOP_USER}\"" >> ${HADOOP_HOME}/etc/hadoop/hadoop-env.sh \
    && echo "export HDFS_SECONDARYNAMENODE_USER=\"${HADOOP_USER}\"" >> ${HADOOP_HOME}/etc/hadoop/hadoop-env.sh \
    && echo "export YARN_RESOURCEMANAGER_USER=\"${HADOOP_USER}\"" >> ${HADOOP_HOME}/etc/hadoop/hadoop-env.sh \
    && echo "export YARN_NODEMANAGER_USER=\"${HADOOP_USER}\"" >> ${HADOOP_HOME}/etc/hadoop/hadoop-env.sh \
    && echo "export HDFS_NAMENODE_OPTS=\"\${HDFS_NAMENODE_OPTS} -Ddfs.namenode.name.dir=file://${HADOOP_HOME}/hdfs/namenode\"" >> ${HADOOP_HOME}/etc/hadoop/hadoop-env.sh \
    && echo "export HDFS_DATANODE_OPTS=\"\${HDFS_DATANODE_OPTS} -Ddfs.datanode.data.dir=file://${HADOOP_HOME}/hdfs/datanode\"" >> ${HADOOP_HOME}/etc/hadoop/hadoop-env.sh

# Create paths
RUN mkdir -p ${HADOOP_HOME}/hdfs/namenode \
    && mkdir -p ${HADOOP_HOME}/hdfs/datanode \
    && mkdir -p ${HADOOP_HOME}/logs \
    && chmod -R 755 ${HADOOP_HOME}/hdfs/

# Download and install Hive
RUN curl -sL --retry 3 "https://www-eu.apache.org/dist/hive/hive-${HIVE_VERSION}/apache-hive-${HIVE_VERSION}-bin.tar.gz" \
    | tar -xz -C /home/${HADOOP_USER}/ \
    && mv /home/${HADOOP_USER}/apache-hive-${HIVE_VERSION}-bin ${HIVE_HOME} \
    && ln -s ${HIVE_HOME} /home/${HADOOP_USER}/hive

COPY hive-site.xml ${HIVE_HOME}/conf/hive-site.xml

RUN echo "export JAVA_HOME=/opt/java/openjdk/" >> ${HIVE_HOME}/conf/hive-env.sh \
    && echo "export HADOOP_HOME='${HADOOP_HOME}'" >> ${HIVE_HOME}/conf/hive-env.sh \
    && echo "export HIVE_CONF_DIR='${HIVE_HOME}/conf'" >> ${HIVE_HOME}/conf/hive-env.sh

EXPOSE 50070 50075 50010 50020 50090 8020 9000 9864 9870 10000 10020 19888 8088 8030 8031 8032 8033 8040 8042 22
# 9088 = 9000 = 9083 =
# 9864 = Hadoop webhfs, 9870 = Hadoop, 9999 = Hive Metastore UI, 10000 = Hive Thrift,
# 50010 = Datanode transfer, 50020 = Datanode IPC, 50075  = Datanode UI
#EXPOSE 8088 9000 9083 9864 9870 9999 10000 50010 50020 50075

# FORMATNODES=0 will prevent formatter
ENV FORMATNODES=1

# YARNSTART=0 will prevent yarn scheduler from being launched
ENV YARNSTART=0

# MAPREDUCESTART=0 will prevent mapreduce from being launched
ENV MAPREDUCESTART=0

ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]