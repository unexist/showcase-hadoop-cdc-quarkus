#!/bin/bash
spark-submit --master spark://localhost:7077 \
    --packages org.apache.spark:spark-sql-kafka-0-10_2.12:3.5.1 \
    --conf spark.executorEnv.JAVA_HOME=/opt/java/openjdk \
    --conf spark.yarn.appMasterEnv.JAVA_HOME=/opt/java/openjdk \
    --conf spark.sql.streaming.checkpointLocation=/tmp/checkpoint \
    --name todosink \
    --deploy-mode client \
    --num-executors 1 \
    --class dev.unexist.showcase.todo.TodoSparkSinkSimple \
    ./todo-spark-sink/target/todo-spark-sink-0.1.jar
