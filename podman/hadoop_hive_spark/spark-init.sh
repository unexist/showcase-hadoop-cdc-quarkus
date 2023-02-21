#!/usr/bin/env bash

spark-sql \
    --packages org.apache.iceberg:iceberg-spark-runtime-3.1_2.12:1.1.0 \
    --conf spark.sql.catalog.todo_catalog=org.apache.iceberg.spark.SparkCatalog \
    --conf spark.sql.catalog.todo_catalog.type=hadoop \
    --conf spark.sql.catalog.todo_catalog.warehouse=hdfs://localhost:9000/warehouse \
    --conf hive.metastore.uris=thrift://localhost:9083 \
    -e "CREATE TABLE IF NOT EXISTS todo_catalog.spark.todos (key string, value string) USING iceberg"