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
	PGPASSWORD=$(PG_PASS) psql -h localhost -U $(PG_USER)

# Browser
open-namenode:
	open http://localhost:9870

open-datanode:
	open http://localhost:9864

open-app:
	open http://localhost:8081

open-debezium:
	open http://localhost:8083