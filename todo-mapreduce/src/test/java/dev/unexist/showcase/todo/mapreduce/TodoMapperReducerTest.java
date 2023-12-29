/**
 * @package Showcase-Hadoop-CDC-Quarkus
 * @file Mapper and Reducer Test
 * @copyright 2023-present Christoph Kappel <christoph@unexist.dev>
 * @version $Id: todo-mapreduce/src/test/java/dev/unexist/showcase/todo/mapreduce/TodoMapperReducerTest.java,v
 *         303 2023/12/29 14:48:37 unexist $
 *
 *         This program can be distributed under the terms of the Apache License v2.0.
 *         See the file LICENSE for details.
 **/

package dev.unexist.showcase.todo.mapreduce;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.apache.hadoop.mrunit.mapreduce.MapReduceDriver;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class TodoMapperReducerTest {
    final static String[] RECORD = {
            "{\"title\":\"string\",\"description\":\"string\",\"done\":false,\"dueDate\":{\"start\":\"2021-05-07\"," +
                    "\"due\":\"2021-05-07\"},\"id\":0}",
            "{\"title\":\"string\",\"description\":\"string\",\"done\":false,\"dueDate\":{\"start\":\"2021-05-07\"," +
                    "\"due\":\"2021-05-07\"},\"id\":1}"
    };

    MapDriver<LongWritable, Text, Text, IntWritable> mapDriver;
    ReduceDriver<Text, IntWritable, Text, IntArrayWritable> reduceDriver;
    MapReduceDriver<LongWritable, Text, Text, IntWritable, Text, IntArrayWritable> mapReduceDriver;

    @Before
    public void setUp() {
        TodoMapper mapper = new TodoMapper();
        TodoReducer reducer = new TodoReducer();

        mapDriver = MapDriver.newMapDriver(mapper);
        reduceDriver = ReduceDriver.newReduceDriver(reducer);
        mapReduceDriver = MapReduceDriver.newMapReduceDriver(mapper, reducer);
    }

    @Test
    public void shouldVerifyMapper() throws IOException {
        mapDriver.withInput(new LongWritable(), new Text(RECORD[0]));
        mapDriver.withOutput(new Text("2021-05-07"), new IntWritable(0));
        mapDriver.runTest();
    }

    @Test
    public void shouldVerifyReducer() throws IOException {
        reduceDriver.withInput(new Text("2021-05-07"), Arrays.asList(
                        new IntWritable(0), new IntWritable(1)
                )
        );
        reduceDriver.withOutput(new Text("2021-05-07"),
                new IntArrayWritable(new Integer[] { 0, 1 }));
        reduceDriver.runTest();
    }

    @Test
    public void shouldVerfiyMapAndReduce() throws IOException {
        mapReduceDriver.withInput(new LongWritable(), new Text(RECORD[0]));
        mapReduceDriver.withInput(new LongWritable(), new Text(RECORD[1]));

        mapReduceDriver.withOutput(new Text("2021-05-07"),
                new IntArrayWritable(new Integer[] { 0, 1}));
        mapReduceDriver.runTest();
    }

    @Test
    public void shouldVerifyEmptyCounter() throws IOException {
        mapDriver.withInput(new LongWritable(), new Text(RECORD[0]));
        mapDriver.withOutput(new Text("2021-05-07"), new IntWritable(0));
        mapDriver.runTest();

        assertThat(mapDriver.getCounters()
                .findCounter(TodoMapper.TodoCounter.TotalError).getValue())
                .isEqualTo(0)
                .withFailMessage("Expected 0 counter increment");
    }
}
