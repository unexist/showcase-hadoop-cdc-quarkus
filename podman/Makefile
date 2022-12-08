# Podman
PODNAME := hadoop
PG_USER := postgres
PG_PASS := postgres

pd-machine-init:
	@podman machine init --memory=8192 --cpus=2 --disk-size=20

pd-machine-start:
	@podman machine start

pd-machine-rm:
	@podman machine rm

pd-machine-recreate: pd-machine-rm pd-machine-init pd-machine-start

pd-pod-create:
	@podman pod create -n $(PODNAME) --network bridge \
		-p 5432:5432 \
		-p 9864:9864 -p 9870:9870 -p 8088:8088 -p 9000:9000 \
		-p 8083:8083 \
		-p 8081:8081

pd-pod-rm:
	@podman pod rm -f $(PODNAME)

pd-pod-recreate: pd-pod-rm pd-pod-create

pd-iceberg-build:
	#@git clone https://github.com/memiiso/debezium-server-iceberg.git
	@cp iceberg/Dockerfile debezium-server-iceberg/Dockerfile
	@cat iceberg/application.properties | sed -e "s/%%PG_USER%%/$(PG_USER)/" -e "s/%%PG_PASS%%/$(PG_PASS)/" > debezium-server-iceberg/application.properties

	@podman build --format docker -t debezium-iceberg -f debezium-server-iceberg/Dockerfile

pd-images-build: pd-iceberg-build

pd-postgres:
	@podman run -dit --name postgres --pod=$(PODNAME) \
		-e POSTGRES_USER=$(PG_USER) \
		-e POSTGRES_PASSWORD=$(PG_PASS) \
		docker.io/library/postgres:14

pd-iceberg:
	@podman run -dit --name iceberg --pod=$(PODNAME) debezium-iceberg:latest

pd-init: pd-machine-init pd-machine-start pd-pod-create pd-images-build

pd-start: pd-postgres pd-hadoop pd-iceberg

# Postgres
psql:
	PGPASSWORD=$(PG_PASS) psql -h localhost -U $(PG_USER)