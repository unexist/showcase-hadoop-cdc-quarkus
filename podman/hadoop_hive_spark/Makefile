init:
	@spark-sql \
	--packages org.apache.iceberg:iceberg-spark-runtime-3.1_2.12:1.1.0 \
	--conf spark.sql.catalog.todo_catalog=org.apache.iceberg.spark.SparkCatalog \
	--conf spark.sql.catalog.todo_catalog.type=hadoop \
	--conf spark.sql.catalog.todo_catalog.warehouse=hdfs://localhost:9000/warehouse \
	--conf hive.metastore.uris=thrift://localhost:9083 \
	-e "CREATE TABLE IF NOT EXISTS todo_catalog.spark.kafkas (key string, value string) USING iceberg; \
	CREATE TABLE IF NOT EXISTS todo_catalog.spark.todos (id string, description string, done string, due string, startdate string, title string) USING iceberg"

copy:
	@hdfs dfs -put todo-spark-sink-0.1.jar hdfs://localhost:9000/jars

run:
	@spark-submit --master spark://hadoop:7077 \
	--packages org.apache.iceberg:iceberg-spark-runtime-3.3_2.12:1.1.0,org.apache.spark:spark-sql-kafka-0-10_2.12:3.3.2 \
	--conf spark.executorEnv.JAVA_HOME=/opt/java/openjdk \
	--conf spark.yarn.appMasterEnv.JAVA_HOME=/opt/java/openjdk \
	--conf spark.sql.streaming.checkpointLocation=/tmp/checkpoint \
	--name todosink \
	--deploy-mode client \
	--num-executors 1 \
	--class dev.unexist.showcase.todo.TodoSparkSink \
	hdfs://hadoop:9000/jars/todo-spark-sink-0.1.jar


shell:
	@spark-shell --master spark://hadoop:7077 \
	--packages org.apache.iceberg:iceberg-spark-runtime-3.3_2.12:1.1.0 \
	--conf spark.sql.catalog.todo_catalog=org.apache.iceberg.spark.SparkCatalog \
	--conf spark.sql.catalog.todo_catalog.type=hadoop \
	--conf spark.sql.catalog.todo_catalog.warehouse=hdfs://localhost:9000/warehouse

status:
	@spark-submit --master spark://hadoop:7077 --status $(ID)