/**
 * @package Showcase-Hadoop-CDC-Quarkus
 *
 * @file Todo Hadoop repository
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
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Named("hadoop_plain")
public class HadoopTodoRepository implements TodoRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(HadoopTodoRepository.class);
    private final String HADOOP_FILE = "/warehouse/quarkus/plain/todo";

    ObjectMapper mapper;
    Configuration configuration;

    /**
     * Constructor
     */

    public HadoopTodoRepository(@ConfigProperty(name = "hadoop.defaultFS", defaultValue = "") String defaultFS) {
        this.mapper = new ObjectMapper();
        this.configuration = new Configuration();

        this.configuration.set("fs.defaultFS", defaultFS);
        this.configuration.set("dfs.replication", "1");
    }

    @Override
    public boolean add(Todo todo) {
        boolean retVal = false;

        /* Append our todo as string */
        try (FileSystem fileSystem = FileSystem.get(this.configuration)) {
            Path hdfsPath = new Path(HADOOP_FILE);
            FSDataOutputStream fsOut;

            if (fileSystem.exists(hdfsPath)) {
                fsOut = fileSystem.append(hdfsPath);
            } else {
                fsOut = fileSystem.create(hdfsPath);
            }

            OutputStreamWriter outStreamWriter = new OutputStreamWriter(fsOut, StandardCharsets.UTF_8);

            BufferedWriter bufferedWriter = new BufferedWriter(outStreamWriter);

            bufferedWriter.write(mapper.writeValueAsString(todo));
            bufferedWriter.newLine();

            bufferedWriter.close();
            outStreamWriter.close();
            fsOut.close();

            retVal = true;
        } catch (IOException e) {
            LOGGER.error("Cannot write data to HDFS", e);
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

        try (FileSystem fileSystem = FileSystem.get(this.configuration)) {
            Path hdfsPath = new Path(HADOOP_FILE);

            FSDataInputStream inputStream = fileSystem.open(hdfsPath);

            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            String line = null;
            while (null != (line = bufferedReader.readLine())) {
                LOGGER.debug("Read line: %s", line);

                retVal.add(this.mapper.readValue(line, Todo.class));
            }

            bufferedReader.close();
            inputStream.close();
        } catch (IOException e) {
            LOGGER.error("Cannot read data from HDFS: ", e);
        }

        return retVal;
    }

    @Override
    public Optional<Todo> findById(int id) {
        throw new NotImplementedException("Needs to be implemented later");
    }
}
