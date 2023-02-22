/**
 * @package Showcase-Hadoop-CDC-Quarkus
 *
 * @file Todo source
 * @copyright 2023-present Christoph Kappel <christoph@unexist.dev>
 * @version $Id$
 *
 * This program can be distributed under the terms of the Apache License v2.0.
 * See the file LICENSE for details.
 **/
package dev.unexist.showcase.todo.adapter;

import dev.unexist.showcase.todo.domain.todo.Todo;
import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class TodoSource {

    @Inject
    @Channel("todo-created")
    Emitter<Todo> emitter;

    /**
     * Send {@link Todo} to topic
     *
     * @param  todo  A {@link Todo} to convert and send
     **/

    public void send(Todo todo) {
        this.emitter.send(KafkaRecord.of(todo.getId(), todo));
    }
}