# Podman
PODNAME := hadoop
PG_USER := postgres
PG_PASS := postgres

pd-machine-init:
	@podman machine init --memory=16384 --cpus=4 --disk-size=100

pd-machine-start:
	@podman machine start

pd-machine-rm:
	@podman machine rm -f

pd-machine-recreate: pd-machine-rm pd-machine-init pd-machine-start

pd-pod-create:
	@podman pod create -n $(PODNAME) --network bridge \
		-p 5432:5432 \
		-p 9864:9864 -p 9866:9866 -p 9868:9868 -p 9870:9870 -p 8088:8088 -p 9000:9000 -p 50070:50070 \
		-p 8083:8083 \
		-p 8081:8081

pd-pod-rm:
	@podman pod rm -f $(PODNAME)

pd-pod-recreate: pd-pod-rm pd-pod-create

pd-iceberg-build:
	#@git clone https://github.com/memiiso/debezium-server-iceberg.git
	@cp iceberg/Dockerfile debezium-server-iceberg/Dockerfile
	@cat iceberg/application.properties | sed -e "s/%%PG_USER%%/$(PG_USER)/" -e "s/%%PG_PASS%%/$(PG_PASS)/" > debezium-server-iceberg/application.properties

	@podman build --format docker -t custom-debezium-iceberg -f debezium-server-iceberg/Dockerfile

pd-postgres-build:
	@podman build --format docker -t custom-postgres -f postgres/Dockerfile

pd-hadoop-build:
	@podman build --format docker -t custom-hadoop -f hadoop/Dockerfile

pd-build: pd-iceberg-build pd-postgres-build pd-hadoop-build

pd-postgres:
	@podman run -dit --name postgres --pod=$(PODNAME) \
		-e POSTGRES_USER=$(PG_USER) \
		-e POSTGRES_PASSWORD=$(PG_PASS) \
		custom-postgres:latest

pd-iceberg:
	@podman run -dit --name iceberg --pod=$(PODNAME) custom-debezium-iceberg:latest

pd-hadoop:
	@podman run -dit --name hadoop-single --pod=$(PODNAME) custom-hadoop:latest

pd-init: pd-machine-init pd-machine-start pd-pod-create pd-images-build

pd-start: pd-postgres pd-hadoop pd-iceberg

# Postgres
psql:
	PGPASSWORD=$(PG_PASS) psql -h localhost -U $(PG_USER)

# Browser
open-hadoop:
	open http://localhost:9000

open-app:
	open http://localhost:8081

open-debezium:
	open http://localhost:8083