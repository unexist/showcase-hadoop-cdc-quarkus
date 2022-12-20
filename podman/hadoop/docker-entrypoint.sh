#!/bin/bash
set -e

if [ ! -f "/etc/ssh/ssh_host_rsa_key" ]; then
  # Generate fresh RSA key
  doas ssh-keygen -f /etc/ssh/ssh_host_rsa_key -N "" -t rsa
fi
if [ ! -f "/etc/ssh/ssh_host_dsa_key" ]; then
  # Generate fresh DSA key
  doas ssh-keygen -f /etc/ssh/ssh_host_dsa_key -N "" -t dsa
fi

echo "Start sshd"
doas /usr/sbin/sshd -D >/dev/null &

if [[ -z ${FORMATNODES} || ${FORMATNODES} -ne 0 ]]; then
  echo "Format namenode"
  ${HADOOP_HOME}/bin/hdfs namenode -format && echo "Format namenode: OK"

  echo "Format secondarynamenode"
  ${HADOOP_HOME}/bin/hdfs secondarynamenode -format -checkpoint force && echo "Format secondarynamenode: OK"
fi

# Create paths
mkdir -p ${HADOOP_HOME}/hdfs/name
mkdir -p ${HADOOP_HOME}/hdfs/data
mkdir -p ${HADOOP_HOME}/logs
chmod -R 755 ${HADOOP_HOME}/hdfs/

echo "Start dfs nodes"
${HADOOP_HOME}/sbin/start-dfs.sh

echo "YARNSTART = $YARNSTART"

if [[ -z ${YARNSTART} || ${YARNSTART} -ne 0 ]]; then
    echo "Start yarn"
    ${HADOOP_HOME}/sbin/start-yarn.sh
fi

# Setup FS
${HADOOP_HOME}/bin/hdfs dfs -mkdir /tmp
${HADOOP_HOME}/bin/hdfs dfs -mkdir /users
${HADOOP_HOME}/bin/hdfs dfs -mkdir /jars
${HADOOP_HOME}/bin/hdfs dfs -chmod 777 /tmp
${HADOOP_HOME}/bin/hdfs dfs -chmod 777 /users
${HADOOP_HOME}/bin/hdfs dfs -chmod 777 /jars

${HADOOP_HOME}/bin/hdfs dfsadmin -safemode leave

# Keep the container running indefinitely
tail -f ${HADOOP_HOME}/logs/hadoop-*-namenode-*.log
