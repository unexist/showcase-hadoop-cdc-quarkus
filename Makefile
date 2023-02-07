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

beeline-init:
	@beeline -u $(HIVE_JDBC) -n $(HADOOP_USER) \
	-e "CREATE TABLE IF NOT EXISTS todos(id integer, description string, done string, due date, startdate date, title string) ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t' LINES TERMINATED BY '\n' STORED AS TEXTFILE;"

beeline-insert:
	@beeline -u $(HIVE_JDBC) -n $(HADOOP_USER) \
	-e "INSERT INTO todos (id, description, done, due, startdate, title) VALUES ($$RANDOM, 'string', 'f', '2023-01-01', '2023-01-01', 'string');"

beeline-select:
	@beeline -u $(HIVE_JDBC) -n $(HADOOP_USER) -e "SELECT * FROM todos;"

beeline-delete:
	@beeline -u $(HIVE_JDBC) -n $(HADOOP_USER) -e "DELETE FROM todos;"

beeline-debezium:
	@beeline -u $(HIVE_JDBC) -n $(HADOOP_USER) \
	-e "add jar /home/$(HADOOP_USER)/hive/lib/iceberg-hive-runtime-1.1.0.jar;" \
	-e "create external table debezium stored by 'org.apache.iceberg.mr.hive.HiveIcebergStorageHandler' location 'hdfs://localhost:9000/warehouse/debeziumevents/debeziumcdc_showcase_public_todos' TBLPROPERTIES ('iceberg.catalog'='location_based_table')"

# Spark
spark-beeline:
	@spark-beeline -u $(HIVE_JDBC) -n $(HADOOP_USER)

# Browser
open-namenode:
	open http://localhost:9870

open-datanode:
	open http://localhost:9864

open-spark:
	open http://localhost:4040

open-resource:
	open http://localhost:8088

open-app:
	open http://localhost:8081

open-debezium:
	open http://localhost:8083