FROM eclipse-temurin:8-jdk

MAINTAINER Christoph Kappel <christoph@unexist.dev>

RUN apt-get -qq -o Acquire::Max-FutureTime=31536000 update -y \
    && export DEBIAN_FRONTEND=noninteractive \
    && apt-get -qq install -y --no-install-recommends doas curl ssh less vim \
    && apt-get clean

RUN groupadd wheel \
    && useradd -m hduser -G wheel -s /usr/bin/bash \
    && echo "hduser:hduser" | chpasswd \
    && echo "permit nopass :wheel" > /etc/doas.conf

COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
COPY ssh_config /etc/ssh/ssh_config

WORKDIR /home/hduser
USER hduser

RUN ssh-keygen -t rsa -P "" -f ~/.ssh/id_rsa \
    && cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys \
    && chmod 0600 ~/.ssh/authorized_keys

ENV JAVA_HOME="/opt/java/openjdk"

ENV HADOOP_USER="hduser"
ENV HADOOP_VERSION="3.3.4"
ENV HADOOP_HOME="/home/${HADOOP_USER}/hadoop-${HADOOP_VERSION}"

ENV HIVE_VERSION="3.1.3"
ENV HIVE_HOME="/home/${HADOOP_USER}/hive-${HIVE_VERSION}"

ENV SPARK_VERSION="3.3.2"
ENV SPARK_HOME="/home/${HADOOP_USER}/spark-${SPARK_VERSION}"

ENV PATH="${PATH}:${HADOOP_HOME}/bin:${HADOOP_HOME}/sbin:${HIVE_HOME}/bin:${SPARK_HOME}/bin"

# Download and install Hadoop
RUN curl -sL --retry 3 "http://archive.apache.org/dist/hadoop/common/hadoop-${HADOOP_VERSION}/hadoop-${HADOOP_VERSION}.tar.gz" \
    | tar -xz -C /home/${HADOOP_USER}/ \
    && rm -rf ${HADOOP_HOME}/share/doc \
    && ln -s ${HADOOP_HOME} /home/${HADOOP_USER}/hadoop

COPY core-site.xml ${HADOOP_HOME}/etc/hadoop/
COPY hdfs-site.xml ${HADOOP_HOME}/etc/hadoop/
COPY yarn-site.xml ${HADOOP_HOME}/etc/hadoop/
COPY mapred-site.xml ${HADOOP_HOME}/etc/hadoop/

# Set options based on variables
RUN echo "" >> ${HADOOP_HOME}/etc/hadoop/hadoop-env.sh \
    && echo "export JAVA_HOME=\"${JAVA_HOME}\"" >> ${HADOOP_HOME}/etc/hadoop/hadoop-env.sh \
    && echo "export HADOOP_HOME=\"${HADOOP_HOME}\"" >> ${HADOOP_HOME}/etc/hadoop/hadoop-env.sh \
    && echo "export HDFS_NAMENODE_USER=\"${HADOOP_USER}\"" >> ${HADOOP_HOME}/etc/hadoop/hadoop-env.sh \
    && echo "export HDFS_DATANODE_USER=\"${HADOOP_USER}\"" >> ${HADOOP_HOME}/etc/hadoop/hadoop-env.sh \
    && echo "export HDFS_SECONDARYNAMENODE_USER=\"${HADOOP_USER}\"" >> ${HADOOP_HOME}/etc/hadoop/hadoop-env.sh \
    && echo "export YARN_RESOURCEMANAGER_USER=\"${HADOOP_USER}\"" >> ${HADOOP_HOME}/etc/hadoop/hadoop-env.sh \
    && echo "export YARN_NODEMANAGER_USER=\"${HADOOP_USER}\"" >> ${HADOOP_HOME}/etc/hadoop/hadoop-env.sh \
    && echo "export HDFS_NAMENODE_OPTS=\"\${HDFS_NAMENODE_OPTS} -Ddfs.namenode.name.dir=file://${HADOOP_HOME}/hdfs/namenode\"" >> ${HADOOP_HOME}/etc/hadoop/hadoop-env.sh \
    && echo "export HDFS_DATANODE_OPTS=\"\${HDFS_DATANODE_OPTS} -Ddfs.datanode.data.dir=file://${HADOOP_HOME}/hdfs/datanode\"" >> ${HADOOP_HOME}/etc/hadoop/hadoop-env.sh

RUN echo "export JAVA_HOME=\"${JAVA_HOME}\"" >> ${HADOOP_HOME}/etc/hadoop/mapred-env.sh \
    && echo "export HADOOP_MAPRED_HOME=\"${HADOOP_HOME}\"" >> ${HADOOP_HOME}/etc/hadoop/mapred-env.sh \
    && echo "export HADOOP_HOME=\"${HADOOP_HOME}\"" >> ${HADOOP_HOME}/etc/hadoop/mapred-env.sh

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

# Add jars
RUN curl -sL --retry 3 -O "https://search.maven.org/remotecontent?filepath=org/apache/iceberg/iceberg-hive-runtime/1.1.0/iceberg-hive-runtime-1.1.0.jar" \
    && mv iceberg-hive-runtime-1.1.0.jar ${HIVE_HOME}/lib/

RUN echo "" >> ${HIVE_HOME}/conf/hive-env.sh \
    && echo "export JAVA_HOME=\"${JAVA_HOME}\"" >> ${HIVE_HOME}/conf/hive-env.sh \
    && echo "export HADOOP_HOME=\"${HADOOP_HOME}\"" >> ${HIVE_HOME}/conf/hive-env.sh \
    && echo "export HIVE_CONF_DIR='${HIVE_HOME}/conf'" >> ${HIVE_HOME}/conf/hive-env.sh

# Create paths
RUN mkdir -p ${HIVE_HOME}/logs

# Download and install Spark
RUN curl -sL --retry 3 "https://dlcdn.apache.org/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop3.tgz" \
    | tar -xz -C /home/${HADOOP_USER}/ \
    && mv /home/${HADOOP_USER}/spark-${SPARK_VERSION}-bin-hadoop3 ${SPARK_HOME} \
    && ln -s ${SPARK_HOME} /home/${HADOOP_USER}/spark

COPY spark-env.sh ${SPARK_HOME}/conf/
COPY spark-init.sh ${SPARK_HOME}/bin/
COPY slaves ${SPARK_HOME}/conf/
COPY spark-defaults.conf ${SPARK_HOME}/conf/

# Add jars
RUN curl -sL --retry 3 -O "https://repo1.maven.org/maven2/org/apache/iceberg/iceberg-spark-runtime-3.3_2.12/1.1.0/iceberg-spark-runtime-3.3_2.12-1.1.0.jar" \
    && mv iceberg-spark-runtime-3.3_2.12-1.1.0.jar ${SPARK_HOME}/jars/

# The file doesn't end on a blank line
RUN echo "" >> ${SPARK_HOME}/conf/spark-env.sh \
    && echo "export JAVA_HOME=\"${JAVA_HOME}\"" >> ${SPARK_HOME}/conf/spark-env.sh \
    && echo "export HADOOP_CONF_DIR=\"${HADOOP_HOME}/etc/hadoop\"" >> ${SPARK_HOME}/conf/spark-env.sh \
    && echo "spark.executorEnv.JAVA_HOME = /opt/java/openjdk" >> ${SPARK_HOME}/conf/spark-defaults.conf \
    && echo "spark.yarn.appMasterEnv.JAVA_HOME = /opt/java/openjdk/" >> ${SPARK_HOME}/conf/spark-defaults.conf

EXPOSE 22 4040 4041 6066 7077 8020 8030 8031 8032 8033 8040 8042 8088 9000 9864 9870 10000 10020 19888 50010 50020 50030 50070 50075 50090

# FORMATNODES=0 will prevent formatter
ENV FORMATNODES=1

# YARNSTART=0 will prevent yarn scheduler from being launched
ENV YARNSTART=1

# MAPREDUCESTART=0 will prevent mapreduce from being launched
ENV MAPREDUCESTART=1

# SPARKSTART=0 will prevent spark from being launched
ENV SPARKSTART=1

# HIVESTART=0 will prevent hive from being launched
ENV HIVESTART=1

ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]