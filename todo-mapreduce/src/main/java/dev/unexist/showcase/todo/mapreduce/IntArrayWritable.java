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
import org.apache.hadoop.io.Writable;

public class IntArrayWritable extends ArrayWritable {
    public IntArrayWritable() {
        super(IntWritable.class);
    }

    public IntArrayWritable(Integer[] values) {
        super(IntWritable.class);

        IntWritable[] ints = new IntWritable[values.length];

        for (int i = 0; i < values.length; i++) {
            ints[i] = new IntWritable(values[i]);
        }

        set(ints);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Writable[] mine = (Writable[]) this.toArray();
        Writable[] theirs = (Writable[]) ((IntArrayWritable)o).toArray();

        if (mine.length != theirs.length) return false;

        for (int i = 0; i < mine.length; i++) {
            if (((IntWritable)mine[i]).get() != ((IntWritable)theirs[i]).get()) return false;
        }

        return true;
    }
}
