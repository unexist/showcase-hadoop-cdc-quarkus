/**
 * @package Showcase-Hadoop-CDC-Quarkus
 *
 * @file Todo text fixture
 * @copyright 2023-present Christoph Kappel <christoph@unexist.dev>
 * @version $Id$
 *
 * This program can be distributed under the terms of the Apache License v2.0.
 * See the file LICENSE for details.
 **/

package dev.unexist.showcase.todo.infrastructure.persistence;

import dev.unexist.showcase.todo.domain.todo.DueDate;
import dev.unexist.showcase.todo.domain.todo.Todo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TodoFixture {
    public static Todo createTodo() {
        Todo todo = new Todo();

        todo.setId(0);
        todo.setTitle("string");
        todo.setDescription("string");

        DueDate dueDate = new DueDate();

        LocalDate date = LocalDate.from(
                DateTimeFormatter.ofPattern(DueDate.DATE_PATTERN).parse("2021-05-07"));

        dueDate.setStart(date);
        dueDate.setDue(date);

        todo.setDueDate(dueDate);

        return todo;
    }
}
