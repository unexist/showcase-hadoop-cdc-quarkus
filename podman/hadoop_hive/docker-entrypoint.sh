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

# Setup FS/Hive
if [[ -z ${FORMATNODES} || ${FORMATNODES} -ne 0 ]]; then
    echo "Creating directories"
    ${HADOOP_HOME}/bin/hdfs dfs -mkdir /tmp
    ${HADOOP_HOME}/bin/hdfs dfs -mkdir /users
    ${HADOOP_HOME}/bin/hdfs dfs -mkdir /jars
    ${HADOOP_HOME}/bin/hdfs dfs -mkdir /user
    ${HADOOP_HOME}/bin/hdfs dfs -mkdir /user/hive
    ${HADOOP_HOME}/bin/hdfs dfs -mkdir /user/hive/warehouse

    ${HADOOP_HOME}/bin/hdfs dfs -chmod 777 /tmp
    ${HADOOP_HOME}/bin/hdfs dfs -chmod 777 /users
    ${HADOOP_HOME}/bin/hdfs dfs -chmod 777 /jars
    ${HADOOP_HOME}/bin/hdfs dfs -chmod 777 /user/hive/warehouse

    echo "Creating schema"
    ${HIVE_HOME}/bin/schematool -initSchema -dbType derby
else
    echo "Not creating directories and schema"
fi

${HADOOP_HOME}/bin/hdfs dfsadmin -safemode leave

# Keep the container running indefinitely
#tail -f ${HADOOP_HOME}/logs/hadoop-*-namenode-*.log

${HIVE_HOME}/bin/hiveserver2