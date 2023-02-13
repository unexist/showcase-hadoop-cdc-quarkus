PG_USER := postgres
PG_PASS := postgres
HADOOP_USER := hduser
HIVE_JDBC := "jdbc:hive2://localhost:10000/default"

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
	@beeline -u $(HIVE_JDBC) -n $(HADOOP_USER) -e "SELECT * FROM todos;"

beeline-debezium-select:
	@beeline -u $(HIVE_JDBC) -n $(HADOOP_USER) -e "SELECT * FROM debezium;"

beeline-delete:
	@beeline -u $(HIVE_JDBC) -n $(HADOOP_USER) -e "DELETE FROM todos;"

beeline-hive-init:
	@beeline -u $(HIVE_JDBC) -n $(HADOOP_USER) \
	-e "CREATE TABLE IF NOT EXISTS todos(id integer, description string, done string, due date, startdate date, title string) ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t' LINES TERMINATED BY '\n' STORED AS TEXTFILE;"

beeline-debezium-init:
	@beeline -u $(HIVE_JDBC) -n $(HADOOP_USER) \
	-e "ADD JAR /home/$(HADOOP_USER)/hive/lib/iceberg-hive-runtime-1.1.0.jar;" \
	-e "CREATE EXTERNAL TABLE IF NOT EXISTS debezium STORED BY 'org.apache.iceberg.mr.hive.HiveIcebergStorageHandler' LOCATION 'hdfs://localhost:9000/warehouse/debeziumevents/debeziumcdc_showcase_public_todos' TBLPROPERTIES ('iceberg.catalog'='location_based_table')"

beeline-copy:
	@beeline -u $(HIVE_JDBC) -n $(HADOOP_USER) \
	-e "INSERT INTO todos (id, description, done, title) SELECT id, description, done, title FROM debezium;"

beeline-init: beeline-hive-init beeline-debezium-init

# Spark
spark-beeline:
	@spark-beeline -u $(HIVE_JDBC) -n $(HADOOP_USER)

spark-shell:
	@spark-shell --master spark://localhost:7077

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
	open http://localhost:8083