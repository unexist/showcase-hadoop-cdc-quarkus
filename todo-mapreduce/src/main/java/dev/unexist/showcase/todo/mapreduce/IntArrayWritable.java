/**
 * @package Showcase-Hadoop-CDC-Quarkus
 *
 * @file Array writeable for integer
 * @copyright 2023-present Christoph Kappel <christoph@unexist.dev>
 * @version $Id$
 *
 * This program can be distributed under the terms of the Apache License v2.0.
 * See the file LICENSE for details.
 **/

package dev.unexist.showcase.todo.mapreduce;

import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.IntWritable;

public class IntArrayWritable extends ArrayWritable {
    public IntArrayWritable() {
        super(IntWritable.class);
    }

    public IntArrayWritable(IntWritable[] values) {
        super(IntWritable.class, values);
    }
}
