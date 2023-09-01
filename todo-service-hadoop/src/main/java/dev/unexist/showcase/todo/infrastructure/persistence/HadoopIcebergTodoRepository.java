/**
 * @package Showcase-Hadoop-CDC-Quarkus
 *
 * @file Todo Hadoop Iceberg repository
 * @copyright 2023-present Christoph Kappel <christoph@unexist.dev>
 * @version $Id$
 *
 * This program can be distributed under the terms of the Apache License v2.0.
 * See the file LICENSE for details.
 **/

package dev.unexist.showcase.todo.infrastructure.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import dev.unexist.showcase.todo.domain.todo.DueDate;
import dev.unexist.showcase.todo.domain.todo.Todo;
import dev.unexist.showcase.todo.domain.todo.TodoFactory;
import dev.unexist.showcase.todo.domain.todo.TodoRepository;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.IcebergGenerics;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.data.parquet.GenericParquetWriter;
import org.apache.iceberg.hadoop.HadoopTables;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.DataWriter;
import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.parquet.Parquet;
import org.apache.iceberg.types.Types;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.apache.iceberg.types.Types.NestedField.optional;
import static org.apache.iceberg.types.Types.NestedField.required;

@ApplicationScoped
@Named("hadoop_iceberg")
public class HadoopIcebergTodoRepository implements TodoRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(HadoopIcebergTodoRepository.class);
    private final String HADOOP_FILE = "/warehouse/quarkus/iceberg/todo";

    ObjectMapper mapper;
    Configuration configuration;
    Schema todoSchema;
    Table todoTable;

    /**
     * Constructor
     */

    public HadoopIcebergTodoRepository(@ConfigProperty(name = "hadoop.defaultFS", defaultValue = "") String defaultFS) {
        this.mapper = new ObjectMapper();

        /* Hadoop configuration */
        this.configuration = new Configuration();

        this.configuration.set("fs.defaultFS", defaultFS);

        /* Iceberg configuration */
        this.todoSchema = new Schema(
                required(1, "id", Types.IntegerType.get()),
                required(2, "title", Types.StringType.get()),
                optional(3, "description", Types.StringType.get()),
                required(4, "done", Types.BooleanType.get()),
                optional(5, "start_date", Types.StringType.get()),
                optional(6, "due_date", Types.StringType.get()));

        HadoopTables tables = new HadoopTables(this.configuration);

        this.todoTable = tables.create(this.todoSchema, PartitionSpec.unpartitioned(), HADOOP_FILE);
    }

    @Override
    public boolean add(Todo todo) {
        boolean retVal = false;

        String filepath = String.format("%s/%s", todoTable.location(), UUID.randomUUID());

        try (FileIO fileIO = todoTable.io()) {
            DataWriter<GenericRecord> dataWriter = Parquet.writeData(fileIO.newOutputFile(filepath))
                        .schema(this.todoSchema)
                        .createWriterFunc(GenericParquetWriter::buildWriter)
                        .withSpec(PartitionSpec.unpartitioned())
                        .build();

            dataWriter.write(this.convertTodoToRecord(todo));
            dataWriter.close();

            todoTable.newAppend().appendFile(dataWriter.toDataFile()).commit();

            retVal = true;
        } catch (IOException e) {
            LOGGER.error("Cannot write Iceberg data to HDFS", e);
        }

        return retVal;
    }

    @Override
    public boolean update(Todo todo) {
        throw new NotImplementedException("Needs to be implemented later");
    }

    @Override
    public boolean deleteById(int id) {
        throw new NotImplementedException("Needs to be implemented later");
    }

    @Override
    public List<Todo> getAll() {
        List<Todo> retVal = new java.util.ArrayList<>();

        try (CloseableIterable<Record> records = IcebergGenerics.read(this.todoTable).build()) {
            for (Record r : records) {
                LOGGER.debug(r.toString());

                retVal.add(TodoFactory.createTodoFromData(
                        r.get(0, Integer.class),
                        r.get(1, String.class),
                        r.get(2, String.class),
                        r.get(3, Boolean.class),
                        r.get(4, String.class),
                        r.get(5, String.class)));
            }
        } catch (IOException e) {
            LOGGER.error("Cannot read Iceberg data from HDFS: ", e);
        }

        return retVal;
    }

    @Override
    public Optional<Todo> findById(int id) {
        throw new NotImplementedException("Needs to be implemented later");
    }

    /**
     * Convert {@link Todo} to a {@link GenericRecord}
     *
     * @param  todo  A {@link Todo} to convert
     *
     * @return A newly created {@link GenericRecord}
     **/

    private GenericRecord convertTodoToRecord(Todo todo) {
        Objects.requireNonNull(todo, "Todo cannot be null");

        GenericRecord record = GenericRecord.create(this.todoSchema);

        return record.copy(ImmutableMap.of("id", todo.getId() ,
                "title", todo.getTitle(),
                "description", todo.getDescription(),
                "done", todo.getDone(),
                "start_date", todo.getDueDate().getStart()
                        .format(DateTimeFormatter.ofPattern(DueDate.DATE_PATTERN)),
                "due_date", todo.getDueDate().getStart()
                        .format(DateTimeFormatter.ofPattern(DueDate.DATE_PATTERN))));
    }
}
