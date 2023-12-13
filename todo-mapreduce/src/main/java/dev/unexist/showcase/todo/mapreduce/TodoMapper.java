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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import dev.unexist.showcase.todo.domain.todo.Todo;

import java.util.stream.Stream;

public class TodoMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
    private Text status = new Text();
    private final static IntWritable addOne = new IntWritable(1);
    private ObjectMapper mapper = new ObjectMapper();

    protected void map(LongWritable key, Text value, Context context)
            throws java.io.IOException, InterruptedException {

        /* Sample record format:
         * {"title":"string","description":"string","done":false,"dueDate":{"start":"2021-05-07","due":"2021-05-07"},"id":0}
         */
        Stream<String> lines = value.toString().lines();

        lines.map(todo -> { this.mapper.readValue(todo, Todo.class) })

        if (Integer.parseInt(line[1]) == 1) {
            status.set(line[4]);
            context.write(status, addOne);
        }
    }
}
