#!/bin/bash
set -e

echo "Starting sshd"
doas service ssh start

# Format namenodes
if [[ -z ${FORMATNODES} || ${FORMATNODES} -ne 0 ]]; then
    echo "Formatting namenode"
    ${HADOOP_HOME}/bin/hdfs namenode -format

    #echo "Formatting secondarynamenode"
    #${HADOOP_HOME}/bin/hdfs secondarynamenode -format -checkpoint force
else
    echo "Not formatting nodes"
fi

echo "Starting nodes"
${HADOOP_HOME}/sbin/start-dfs.sh

if [[ -z ${YARNSTART} || ${YARNSTART} -ne 0 ]]; then
    echo "Starting yarn"
    ${HADOOP_HOME}/sbin/start-yarn.sh
else
    echo "Not starting yarn"
fi

if [[ -z ${MAPREDUCESTART} || ${MAPREDUCESTART} -ne 0 ]]; then
    echo "Starting historyserver"
    ${HADOOP_HOME}/bin/mapred --daemon start historyserver
else
    echo "Not starting historyserver"
fi

if [[ -z ${SPARKSTART} || ${SPARKSTART} -ne 0 ]]; then
    echo "Starting spark"
    ${SPARK_HOME}/sbin/start-all.sh
else
    echo "Not starting spark"
fi

# Setup FS/Hive
if [[ -z ${FORMATNODES} || ${FORMATNODES} -ne 0 ]]; then
    echo "Creating directories"
    ${HADOOP_HOME}/bin/hdfs dfs -mkdir -p /tmp
    ${HADOOP_HOME}/bin/hdfs dfs -mkdir -p /users
    ${HADOOP_HOME}/bin/hdfs dfs -mkdir -p /jars
    ${HADOOP_HOME}/bin/hdfs dfs -mkdir -p /warehouse/hive

    ${HADOOP_HOME}/bin/hdfs dfs -chmod 777 /tmp
    ${HADOOP_HOME}/bin/hdfs dfs -chmod 777 /users
    ${HADOOP_HOME}/bin/hdfs dfs -chmod 777 /jars
    ${HADOOP_HOME}/bin/hdfs dfs -chmod 777 /warehouse
    ${HADOOP_HOME}/bin/hdfs dfs -chmod 777 /warehouse/hive

    echo "Creating schema"
    ${HIVE_HOME}/bin/schematool -initSchema -dbType derby
else
    echo "Not creating directories and schema"
fi

${HADOOP_HOME}/bin/hdfs dfsadmin -safemode leave

# Keep the container running indefinitely
#tail -f ${HADOOP_HOME}/logs/hadoop-*-namenode-*.log

${HIVE_HOME}/bin/hiveserver2