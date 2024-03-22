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

//import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import org.apache.spark.SparkConf
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.{SparkSession, functions}

import java.util.concurrent.TimeUnit

object TodoSparkSinkSimple {
  def main(args: Array[String]): Unit = {

    /* Configure parameters */
    val sparkConf = new SparkConf()

    sparkConf.set("packages", "org.apache.spark:spark-sql-kafka-0-10_2.12:3.5.1")

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

    /* Field annotations just work for the direct field */
    //@SuppressFBWarnings(value = Array("BC_UNCONFIRMED_CAST_OF_RETURN_VALUE"), justification = "I don't know what I am doing")
    val dataFrame = df.selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
    val resDF = dataFrame.as[(String, String)].toDF("key", "value")

    /* Write data to the console in streaming mode every minute */
    resDF.writeStream // <4>
      .format("console")
      .outputMode("update")
      .trigger(Trigger.ProcessingTime(1, TimeUnit.MINUTES))
      .start()

    /* Reset the last terminated streaming query to avoid an immediate return */
    spark.streams.resetTerminated()

    /* Wait for the remaining one to terminate */
    spark.streams.awaitAnyTermination()
  }
}