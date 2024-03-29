/**
 * @package Showcase-Hadoop-CDC-Quarkus
 *
 * @file Spark sink
 * @copyright 2023-present Christoph Kappel <christoph@unexist.dev>
 * @version $Id$
 *
 * This program can be distributed under the terms of the Apache License v2.0.
 * See the file LICENSE for details.
 **/

package dev.unexist.showcase.todo

import org.apache.spark.SparkConf
import org.apache.spark.sql.{SparkSession, functions}
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.streaming.Trigger

import java.util.concurrent.TimeUnit

object TodoSparkSinkAsIceberg {
    def main(args: Array[String]): Unit = {

        /* Configure parameters */
        val sparkConf = new SparkConf()
            .set("packages", "org.apache.iceberg:iceberg-spark-runtime-3.5_2.13:1.1.0,org.apache.spark:spark-sql-kafka-0-10_2.12:3.5.1")
            .set("spark.sql.catalog.todo_catalog", "org.apache.iceberg.spark.SparkCatalog")
            .set("spark.sql.catalog.todo_catalog.type", "hadoop")
            .set("spark.sql.catalog.todo_catalog.warehouse", "hdfs://localhost:9000/warehouse")

        val spark = SparkSession
            .builder()
            .config(sparkConf)
            .appName("TodoSparkSink")
            .getOrCreate()

        import spark.implicits._

        /* Read data from Kafka */
        val df = spark.readStream
            .format("kafka")
            .option("kafka.bootstrap.servers", "localhost:9092")
            .option("subscribe", "todo_created")
            .option("checkpointLocation", "/tmp/checkpoint")
            .load()

        val dataFrame1 = df.selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
        val resDF1 = dataFrame1.as[(String, String)].toDF("key", "value")

        /* Write data to the Iceberg table kafka in streaming mode every minute */
        resDF1.writeStream
            .format("iceberg")
            .outputMode("append")
            .trigger(Trigger.ProcessingTime(1, TimeUnit.MINUTES))
            .option("path", "todo_catalog.spark.messages")
            .start()

        /* Order matters:
         * JSON: {"title":"string","description":"string","done":false,"dueDate":{"start":"2021-05-07","due":"2021-05-07"},"id":1}
         * Table: (id string, description string, done string, due string, startdate string, title string)
         */

        /* Field annotations just work for the direct field */
        //@SuppressFBWarnings(value = Array("BC_UNCONFIRMED_CAST_OF_RETURN_VALUE"), justification = "I don't know what I am doing")
        val resDF2 = dataFrame1.as[(String, String)].toDF("key", "value")
            .withColumn("id", col("key"))
            .withColumn("description", functions.split(col("value"), ",").getItem(1))
            .withColumn("done", functions.split(col("value"), ",").getItem(2))
            .withColumn("due", functions.split(col("value"), ",").getItem(3))
            .withColumn("startdate", functions.split(col("value"), ",").getItem(4))
            .withColumn("title", functions.split(col("value"), ",").getItem(0))
            .drop("key")
            .drop("value")

        /* Write data to the Iceberg table in streaming mode every minute */
        resDF2.writeStream
            .format("iceberg")
            .outputMode("append")
            .trigger(Trigger.ProcessingTime(1, TimeUnit.MINUTES))
            .option("path", "todo_catalog.spark.todos")
            .start()

        /* Wait for the first stream writer to terminate */
        spark.streams.awaitAnyTermination()

        /* Reset the last terminated streaming query to avoid an immediate return */
        spark.streams.resetTerminated()

        /* Wait for the remaining one to terminate */
        spark.streams.awaitAnyTermination()
    }
}