/**
 * @package Showcase-Hadoop-CDC-Quarkus
 *
 * @file Todo reducer
 * @copyright 2023-present Christoph Kappel <christoph@unexist.dev>
 * @version $Id$
 *
 * This program can be distributed under the terms of the Apache License v2.0.
 * See the file LICENSE for details.
 **/

package dev.unexist.showcase.todo.mapreduce;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TodoReducer extends Reducer<Text, IntWritable, Text, IntArrayWritable> {

    protected void reduce(Text key, Iterable<IntWritable> values, Context context) throws java.io.IOException,
            InterruptedException
    {
        List<Integer> idList = new ArrayList<>();

        for (IntWritable value : values) {
            idList.add(value.get());
        }

        context.write(key, new IntArrayWritable(idList.toArray(Integer[]::new)));
    }
}
