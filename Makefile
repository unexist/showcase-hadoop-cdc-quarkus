PG_USER := postgres
PG_PASS := postgres
HADOOP_USER := hduser
HIVE_JDBC := "jdbc:hive2://localhost:10000/default"
SPARK_DEPLOY_MODE := cluster

define JSON_TODO
curl -X 'POST' \
  'http://localhost:8080/todo' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "description": "string",
  "done": true,
  "dueDate": {
    "due": "2021-05-07",
    "start": "2021-05-07"
  },
  "title": "string"
}'
endef
export JSON_TODO

# Tools
todo:
	@echo $$JSON_TODO | bash

list:
	@curl -X "GET" "http://localhost:8080/todo" -H 'accept: */*' | jq .

report:
	@hdfs dfsadmin -fs hdfs://localhost:9000 -report

upload:
	@hdfs dfs -put dump.sql hdfs://localhost:9000/tmp

copy: CONTAINERID = $(shell podman container ls | grep hadoop | awk '{print $$1}')
copy:
	@podman cp todo-spark-sink/target/todo-spark-sink-0.1.jar $(CONTAINERID):/home/hduser

# Postgres
psql:
	@PGPASSWORD=$(PG_PASS) psql -h localhost -U $(PG_USER)

psql-dump:
	@PGPASSWORD=$(PG_PASS) pg_dump -h localhost -U $(PG_PASS) --data-only --table=todos | \grep -E "^[0-9]+.*" > dump.sql

# Beeline
beeline:
	@beeline -u $(HIVE_JDBC) -n $(HADOOP_USER)

beeline-insert:
	@beeline -u $(HIVE_JDBC) -n $(HADOOP_USER) \
	-e "INSERT INTO todos (id, description, done, due, startdate, title) VALUES ($$RANDOM, 'string', 'f', '2023-01-01', '2023-01-01', 'string');"

beeline-hive-select:
	@beeline -u $(HIVE_JDBC) -n $(HADOOP_USER) -e "SELECT * FROM hive_todos;"

beeline-debezium-select:
	@beeline -u $(HIVE_JDBC) -n $(HADOOP_USER) -e "SELECT * FROM debezium_todos;"

beeline-spark-select:
	@beeline -u $(HIVE_JDBC) -n $(HADOOP_USER) -e "SELECT * FROM spark_todos;"

beeline-delete:
	@beeline -u $(HIVE_JDBC) -n $(HADOOP_USER) -e "DELETE FROM hive_todos;"

beeline-hive-init:
	@beeline -u $(HIVE_JDBC) -n $(HADOOP_USER) \
	-e "CREATE TABLE IF NOT EXISTS hive_todos(id integer, description string, done string, due date, startdate date, title string) ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t' LINES TERMINATED BY '\n' STORED AS TEXTFILE;"

beeline-debezium-init:
	@beeline -u $(HIVE_JDBC) -n $(HADOOP_USER) \
	-e "ADD JAR /home/$(HADOOP_USER)/hive/lib/iceberg-hive-runtime-1.1.0.jar;" \
	-e "CREATE EXTERNAL TABLE IF NOT EXISTS debezium_todos STORED BY 'org.apache.iceberg.mr.hive.HiveIcebergStorageHandler' LOCATION 'hdfs://localhost:9000/warehouse/debezium/debeziumcdc_showcase_public_todos' TBLPROPERTIES ('iceberg.catalog'='location_based_table')"

beeline-spark-init:
	@beeline -u $(HIVE_JDBC) -n $(HADOOP_USER) \
	-e "ADD JAR /home/$(HADOOP_USER)/hive/lib/iceberg-hive-runtime-1.1.0.jar;" \
	-e "CREATE EXTERNAL TABLE IF NOT EXISTS spark_todos STORED BY 'org.apache.iceberg.mr.hive.HiveIcebergStorageHandler' LOCATION 'hdfs://localhost:9000/warehouse/spark/todos' TBLPROPERTIES ('iceberg.catalog'='location_based_table')"

beeline-copy:
	@beeline -u $(HIVE_JDBC) -n $(HADOOP_USER) \
	-e "INSERT INTO todos (id, description, done, title) SELECT id, description, done, title FROM debezium;"

# Spark
spark-beeline:
	@spark-beeline -u $(HIVE_JDBC) -n $(HADOOP_USER)

# Pay attention to the Spark and Scala versions, they must match Spark
spark-shell:
	@spark-shell --master spark://localhost:7077 \
	--packages org.apache.iceberg:iceberg-spark-runtime-3.3_2.12:1.1.0 \
	--conf spark.sql.catalog.todo_catalog=org.apache.iceberg.spark.SparkCatalog \
	--conf spark.sql.catalog.todo_catalog.type=hadoop \
	--conf spark.sql.catalog.todo_catalog.warehouse=hdfs://localhost:9000/warehouse

spark-submit:
	@spark-submit --master spark://localhost:7077 \
	--packages org.apache.iceberg:iceberg-spark-runtime-3.3_2.12:1.1.0,org.apache.spark:spark-sql-kafka-0-10_2.12:3.3.2 \
	--conf spark.executorEnv.JAVA_HOME=/opt/java/openjdk \
	--conf spark.yarn.appMasterEnv.JAVA_HOME=/opt/java/openjdk \
	--conf spark.sql.streaming.checkpointLocation=/tmp/checkpoint \
	--name todosink \
	--deploy-mode $(SPARK_DEPLOY_MODE) \
	--num-executors 1 \
	--class dev.unexist.showcase.todo.TodoSparkSink \
	hdfs://localhost:9000/jars/todo-spark-sink-0.1.jar

spark-status:
	@spark-submit --master spark://localhost:7077 --status $(ID)

# Init
data-init: todo beeline-hive-init beeline-debezium-init beeline-spark-init

# Kafkacat
kat-test:
	@kcat -t todo_created -b localhost:9092 -P

kat-listen:
	@kcat -t todo_created -b localhost:9092 -C

# Browser
open-namenode:
	open http://localhost:9870

open-datanode:
	open http://localhost:9864

open-spark-master:
	open http://localhost:4040

open-spark-slave:
	open http://localhost:4041

open-spark-shell:
	open http://localhost:4042

open-resourcemanager:
	open http://localhost:8088

open-app:
	open http://localhost:8081

open-debezium:
	open http://localhost:8090