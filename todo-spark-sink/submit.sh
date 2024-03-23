#!/bin/zsh
spark-submit --master spark://${HOST}:7077 \
    --packages org.apache.spark:spark-sql-kafka-0-10_2.12:3.5.1 \
    --conf spark.executorEnv.JAVA_HOME=${JAVA_HOME} \
    --conf spark.yarn.appMasterEnv.JAVA_HOME=${JAVA_HOME} \
    --conf spark.sql.streaming.checkpointLocation=/tmp/checkpoint \
    --conf spark.dynamicAllocation.enabled=false \
    --name todosink \
    --deploy-mode client \
    --num-executors 1 \
    --executor-cores 1 \
    --driver-memory 1G \
    --executor-memory 1G \
    --class dev.unexist.showcase.todo.TodoSparkSinkToConsole \
    ./target/todo-spark-sink-0.1.jar
