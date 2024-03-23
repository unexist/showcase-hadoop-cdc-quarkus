# Podman
PODNAME := hadoop
PG_USER := postgres
PG_PASS := postgres

pd-machine-create:
	@podman machine init --memory=16384 --cpus=4 --disk-size=100

pd-machine-start:
	@podman machine start

pd-machine-rm:
	@podman machine rm -f

pd-machine-recreate: pd-machine-rm pd-machine-create pd-machine-start

pd-pod-create:
# 4040 = Spark master web port
# 4041 = Spark slave web port
# 6066 = Spark REST port
# 7077 = Spark master port
# 8088 = Hadoop Resource Manager
# 8080 = Quarkus/Spark master
# 8081 = Spark worker
# 8090 = Iceberg
# 9088 =
# 9000 =
# 9083 = Hive thrift metastore
# 9864 = Hadoop datanode web port
# 9867 = Hadoop datanode IPC port
# 9870 = Hadoop namenode web port
# 9092 = Kafka port
# 9999 = Hive Metastore UI
# 10000 = Hive Thrift
# 50010 = Datanode transfer
# 50020 = Datanode IPC
# 50075 = Datanode UI
# 50030 = Jobtracker port
	@podman pod create -n $(PODNAME) --network bridge \
		-p 5432:5432 \
		-p 8080:8080 \
		-p 8081:8081 \
		-p 8090:8090 \
		-p 9092:9092 \
		-p 4040:4040 -p 4041:4041 -p 6066:6066 -p 7077:7077 -p 8020:8020 -p 8088:8088 -p 9000:9000 -p 9083:9083 -p 9864:9864 -p 9870:9870 -p 9999:9999 -p 10000:10000 -p 50030:50030

pd-pod-create-kafka:
	@podman pod create -n $(PODNAME) --network bridge -p 9092:9092

pd-pod-rm:
	@podman pod rm -f $(PODNAME)

pd-pod-recreate: pd-pod-rm pd-pod-create

pd-iceberg-build:
	@test ! -d "debezium-server-iceberg" && git clone https://github.com/memiiso/debezium-server-iceberg.git || echo
	@cp iceberg/Dockerfile debezium-server-iceberg/Dockerfile
	@cat iceberg/application.properties | sed -e "s/%%PG_USER%%/$(PG_USER)/" -e "s/%%PG_PASS%%/$(PG_PASS)/" > debezium-server-iceberg/application.properties

	@podman build --format docker -t custom-debezium-iceberg -f debezium-server-iceberg/Dockerfile

pd-postgres-build:
	@podman build --format docker -t custom-postgres -f postgres/Dockerfile

pd-hadoop-hive-spark-build:
	@podman build --format docker -t custom-hadoop-hive-spark -f hadoop_hive_spark/Dockerfile

pd-spark-build:
	@podman build --format docker -t custom-spark -f spark/Dockerfile

pd-quarkus-build:
	mvn -f ../todo-service-debezium/pom.xml clean package -Dquarkus.container-image.build=true #-Dquarkus.container-image.push=true

pd-build: pd-iceberg-build pd-postgres-build pd-hadoop-hive-spark-build pd-spark-build pd-quarkus-build

pd-postgres:
	@podman run -dit --name postgres --pod=$(PODNAME) \
		-e POSTGRES_USER=$(PG_USER) \
		-e POSTGRES_PASSWORD=$(PG_PASS) \
		custom-postgres:latest

pd-iceberg:
	@podman run -dit --name iceberg --pod=$(PODNAME) custom-debezium-iceberg:latest

pd-hadoop:
	@podman run -dit --name hadoop-single --pod=$(PODNAME) custom-hadoop:latest

pd-hadoop-hive-spark:
	@podman run -dit --name hadoop-hive-spark-single --cap-add SYS_CHROOT --pod=$(PODNAME) custom-hadoop-hive-spark:latest

pd-spark:
	@podman run -dit --name spark-single --cap-add SYS_CHROOT --pod=$(PODNAME) custom-spark:latest

pd-redpanda:
	@podman run -dit --name redpanda --pod=$(PODNAME) docker.io/vectorized/redpanda:latest

pd-quarkus:
	@podman run -dit --name quarkus --pod=$(PODNAME) todo-service-debezium:0.1

pd-init: pd-machine-init pd-machine-start pd-pod-create pd-images-build

pd-start: pd-redpanda pd-postgres pd-hadoop-hive-spark pd-iceberg pd-quarkus

# Iceberg
iceberg:
	#cp iceberg/application.properties debezium-server-iceberg/debezium-server-iceberg-dist/target/debezium-server-iceberg/conf/
	debezium-server-iceberg/debezium-server-iceberg-dist/target/debezium-server-iceberg/run.sh