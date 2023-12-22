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

import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TodoReducer extends Reducer<Text, IntWritable, Text, TodoReducer.IntArrayWritable> {

    protected void reduce(Text key, Iterable<IntWritable> values, Context context) throws java.io.IOException,
            InterruptedException
    {
        List<IntWritable> ids = new ArrayList<>();

        for (IntWritable value : values) {
            ids.add(value);
        }

        context.write(key, new IntArrayWritable(ids.toArray(IntWritable[]::new)));
    }

    public static class IntArrayWritable extends ArrayWritable {
        public IntArrayWritable() {
            super(IntWritable.class);
        }

        public IntArrayWritable(IntWritable[] intWritables) {
            super(IntWritable.class, intWritables);
        }

        @Override
        public IntWritable[] get() {
            return (IntWritable[]) super.get();
        }

        @Override
        public void write(DataOutput dataOutput) throws IOException {
            for(IntWritable data : get()){
                data.write(dataOutput);
            }
        }
    }
}
