#!/bin/bash
set -e

echo "Starting master"
${SPARK_HOME}/sbin/start-master.sh

if [[ -z ${WORKERSTART} || ${WORKERSTART} -ne 0 ]]; then
    echo "Starting worker"
    ${SPARK_HOME}/sbin/start-worker.sh spark://localhost:7070
else
    echo "Not starting worker"
fi

# Keep the container running indefinitely
tail -f ${SPARK_HOME}/logs/spark-*.out