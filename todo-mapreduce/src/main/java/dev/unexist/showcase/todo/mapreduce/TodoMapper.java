/**
 * @package Showcase-Hadoop-CDC-Quarkus
 *
 * @file Todo mapper
 * @copyright 2023-present Christoph Kappel <christoph@unexist.dev>
 * @version $Id$
 *
 * This program can be distributed under the terms of the Apache License v2.0.
 * See the file LICENSE for details.
 **/

package dev.unexist.showcase.todo.mapreduce;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.unexist.showcase.todo.domain.todo.DueDate;
import dev.unexist.showcase.todo.domain.todo.Todo;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.time.format.DateTimeFormatter;

public class TodoMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
    private final ObjectMapper mapper = new ObjectMapper();

    enum TodoCounter {
       TotalError;
     };

    protected void map(LongWritable key, Text value, Context context)
            throws java.io.IOException, InterruptedException
    {
        try {
            Todo todo = this.mapper.readValue(value.toString(), Todo.class);

            String formattedDate = todo.getDueDate().getDue()
                    .format(DateTimeFormatter.ofPattern(DueDate.DATE_PATTERN));

            context.write(new Text(formattedDate), new IntWritable(todo.getId()));
        } catch (JsonProcessingException e) {
            context.getCounter(TodoCounter.TotalError).increment(1);
        }
    }
}
