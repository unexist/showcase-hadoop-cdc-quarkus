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
import dev.unexist.showcase.todo.domain.todo.Todo;
import dev.unexist.showcase.todo.domain.todo.TodoRepository;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.apache.iceberg.hadoop.HadoopTables;
import org.apache.iceberg.types.Types;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.util.List;
import java.util.Optional;

import static org.apache.iceberg.types.Types.NestedField.optional;

@ApplicationScoped
@Named("hadoop_iceberg")
public class HadoopIcebergTodoRepository implements TodoRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(HadoopIcebergTodoRepository.class);
    private final String HADOOP_FILE = "/warehouse/quarkus/iceberg/todo";

    ObjectMapper mapper;
    Configuration configuration;
    Schema todoSchema;

    /**
     * Constructor
     */

    public HadoopIcebergTodoRepository(@ConfigProperty(name = "hadoop.defaultFS", defaultValue = "") String defaultFS) {
        this.mapper = new ObjectMapper();

        /* Hadoop configuration */
        this.configuration = new Configuration();

        this.configuration.set("fs.defaultFS", defaultFS);

        /* Iceberg configuration */
        this.todoSchema = new Schema(optional(1, "id", Types.IntegerType.get()),
                optional(2, "title", Types.StringType.get()),
                optional(3, "description", Types.StringType.get()),
                optional(4, "done", Types.BooleanType.get()));
    }

    @Override
    public boolean add(Todo todo) {
        boolean retVal = false;

        PartitionSpec todoSpec = PartitionSpec.builderFor(this.todoSchema)
                .identity("id").build();

        HadoopTables tables = new HadoopTables(this.configuration);

        Table table = tables.create(this.todoSchema, todoSpec, HADOOP_FILE);

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
        throw new NotImplementedException("Needs to be implemented later");
    }

    @Override
    public Optional<Todo> findById(int id) {
        throw new NotImplementedException("Needs to be implemented later");
    }
}
