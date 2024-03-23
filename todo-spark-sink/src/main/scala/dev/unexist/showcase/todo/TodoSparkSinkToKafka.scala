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
import org.apache.spark.sql.SparkSession

object TodoSparkSinkToKafka {
    def main(args: Array[String]): Unit = {

        /* Configure parameters */
        val sparkConf = new SparkConf()
            .set("packages", "org.apache.spark:spark-sql-kafka-0-10_2.12:3.5.1")
            .set("spark.cores.max", "1")

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

        df.printSchema()

        /* Assemble to new key-value pair */
        val dataFrame = df.selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
        val resDF = dataFrame.as[(String, String)].toDF("key", "value")

        /* Write data to the console in streaming mode every minute */
        resDF.writeStream
            .format("kafka")
            .outputMode("append")
            .option("kafka.bootstrap.servers", "localhost:9092")
            .option("topic", "todo_streamed")
            .start()
            .awaitTermination()
    }
}