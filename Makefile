PG_USER := postgres
PG_PASS := postgres

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


psql:
	@PGPASSWORD=$(PG_PASS) psql -h localhost -U $(PG_USER)

dump:
	@PGPASSWORD=$(PG_PASS) pg_dump -h localhost -U $(PG_PASS) --data-only --table=todos | \grep -E "^[0-9]+.*" > dump.sql

beeline:
	@beeline -u "jdbc:hive2://localhost:10000/default"

beeline-init:
	@beeline -u "jdbc:hive2://localhost:10000/default" -e \
	"create table if not exists todos(id integer, description string, done string, due date, startdate date, title string) row format delimited fields terminated by '\t' lines terminated by '\n' stored as textfile;"

report:
	@hdfs dfsadmin -fs hdfs://localhost:9000 -report

upload:
	@hdfs dfs -put dump.sql hdfs://localhost:9000/tmp

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