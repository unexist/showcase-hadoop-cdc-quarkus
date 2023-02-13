import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.Trigger

import java.util.concurrent.TimeUnit

def main(args: Array[String]): Unit = {

  /* Configure the parameters for the catalog */
  val sparkConf = new SparkConf()
  
  sparkConf.set("spark.sql.extensions", "org.apache.iceberg.spark.extensions.IcebergSparkSessionExtensions")
  sparkConf.set("spark.sql.catalog.dlf_catalog", "org.apache.iceberg.spark.SparkCatalog")
  sparkConf.set("spark.sql.catalog.dlf_catalog.catalog-impl", "org.apache.iceberg.aliyun.dlf.DlfCatalog")
  sparkConf.set("spark.sql.catalog.dlf_catalog.io-impl", "org.apache.iceberg.hadoop.HadoopFileIO")
  sparkConf.set("spark.sql.catalog.dlf_catalog.oss.endpoint", "<yourOSSEndpoint>")
  sparkConf.set("spark.sql.catalog.dlf_catalog.warehouse", "<yourOSSWarehousePath>")
  sparkConf.set("spark.sql.catalog.dlf_catalog.access.key.id", "<yourAccessKeyId>")
  sparkConf.set("spark.sql.catalog.dlf_catalog.access.key.secret", "<yourAccessKeySecret>")
  sparkConf.set("spark.sql.catalog.dlf_catalog.dlf.catalog-id", "<yourCatalogId>")
  sparkConf.set("spark.sql.catalog.dlf_catalog.dlf.endpoint", "<yourDLFEndpoint>")
  sparkConf.set("spark.sql.catalog.dlf_catalog.dlf.region-id", "<yourDLFRegionId>")

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