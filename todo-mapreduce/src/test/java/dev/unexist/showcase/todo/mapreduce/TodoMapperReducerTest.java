/**
 * @package Showcase-Hadoop-CDC-Quarkus
 *
 * @file Mapper and Reducer Test
 * @copyright 2023-present Christoph Kappel <christoph@unexist.dev>
 * @version $Id$
 *
 * This program can be distributed under the terms of the Apache License v2.0.
 * See the file LICENSE for details.
 **/

package dev.unexist.showcase.todo.mapreduce;

import dev.unexist.showcase.todo.mapreduce.TodoMapper;
import dev.unexist.showcase.todo.mapreduce.TodoReducer;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.MapReduceDriver;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.junit.Before;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TodoMapperReducerTest {
    MapDriver<LongWritable, Text, Text, IntWritable> mapDriver;
    ReduceDriver<Text, IntWritable, Text, IntWritable> reduceDriver;
    MapReduceDriver<LongWritable, Text, Text, IntWritable, Text, IntWritable> mapReduceDriver;

    @Before
    public void setUp() {
        TodoMapper mapper = new TodoMapper();
        TodoReducer reducer = new TodoReducer();

        mapDriver = MapDriver.newMapDriver(mapper);
        reduceDriver = ReduceDriver.newReduceDriver(reducer);
        mapReduceDriver = MapReduceDriver.newMapReduceDriver(mapper, reducer);
    }

    @Test
    public void testMapper() throws IOException {
        mapDriver.withInput(new LongWritable(), new Text(
                "655209;1;796764372490213;804422938115889;6"));
        mapDriver.withOutput(new Text("6"), new IntWritable(1));
        mapDriver.runTest();
    }

    @Test
    public void testReducer() throws IOException {
        List<IntWritable> values = new ArrayList<IntWritable>();

        values.add(new IntWritable(1));
        values.add(new IntWritable(1));

        reduceDriver.withInput(new Text("6"), values);
        reduceDriver.withOutput(new Text("6"), new IntWritable(2));
        reduceDriver.runTest();
    }

    @Test
    public void testMapReduce() throws IOException {
        mapReduceDriver.withInput(new LongWritable(), new Text(
                "655209;1;796764372490213;804422938115889;6"));

        List<IntWritable> values = new ArrayList<IntWritable>();

        values.add(new IntWritable(1));
        values.add(new IntWritable(1));

        mapReduceDriver.withOutput(new Text("6"), new IntWritable(2));
        mapReduceDriver.runTest();
    }
}
