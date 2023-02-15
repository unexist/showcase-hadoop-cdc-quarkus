import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.Trigger

import java.util.concurrent.TimeUnit

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

  /* Read data from the Kafka cluster */
  val df = spark.readStream
    .format("kafka")
    .option("kafka.bootstrap.servers", "localhost:9092")
    .option("subscribe", "todo_created")
    .load()

  val resDF = df.selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
    .as[(String, String)].toDF("id", "data")

  /* Write data to the Iceberg table in streaming mode */
  val query = resDF.writeStream
    .format("iceberg")
    .outputMode("append")
    .trigger(Trigger.ProcessingTime(1, TimeUnit.MINUTES))
    .option("path", "dlf_catalog.iceberg_db.iceberg_table")
    .start()

  query.awaitTermination()
}