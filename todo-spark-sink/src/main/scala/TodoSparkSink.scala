/**
 * @package Showcase-Hadoop-CDC-Quarkus
 *
 * @file Spark sink
 * @copyright 2020-2022 Christoph Kappel <christoph@unexist.dev>
 * @version $Id$
 *
 * This program can be distributed under the terms of the Apache License v2.0.
 * See the file LICENSE for details.
 **/

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.Trigger

import java.util.concurrent.TimeUnit

object TodoSparkSink {
  def main(args: Array[String]): Unit = {

    /* Configure the parameters for the catalog */
    val sparkConf = new SparkConf()

    sparkConf.set("spark.sql.extensions", "org.apache.iceberg.spark.extensions.IcebergSparkSessionExtensions")
    sparkConf.set("spark.sql.catalog.todo_catalog", "org.apache.iceberg.spark.SparkCatalog")
    sparkConf.set("spark.sql.catalog.todo_catalog.type", "hadoop")
    sparkConf.set("spark.sql.catalog.todo_catalog.warehouse", "hdfs://localhost:9000/warehouse")

    val spark = SparkSession
      .builder()
      .config(sparkConf)
      .appName("TodoSparkSink")
      .getOrCreate()

    import spark.implicits._

    /* Read data from the Kafka cluster */
    val df = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", "todo_created")
      .load()

    /* Field annotations just work for the direct field */
    @SuppressFBWarnings(value = Array("BC_UNCONFIRMED_CAST_OF_RETURN_VALUE"), justification = "I don't know what I am doing")
    val dataFrame = df.selectExpr("CAST(title AS STRING)", "CAST(description AS STRING)")

    val resDF = dataFrame.as[(String, String)].toDF("title", "description")

    /* Write data to the Iceberg table in streaming mode */
    val query = resDF.writeStream
      .format("iceberg")
      .outputMode("append")
      .trigger(Trigger.ProcessingTime(1, TimeUnit.MINUTES))
      .option("path", "todo_catalog.spark.todos")
      .start()

    query.awaitTermination()
  }
}