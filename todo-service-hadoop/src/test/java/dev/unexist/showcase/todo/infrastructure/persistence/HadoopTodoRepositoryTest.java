/**
 * @package Showcase-Hadoop-CDC-Quarkus
 *
 * @file Hadoop repository test
 * @copyright 2023-present Christoph Kappel <christoph@unexist.dev>
 * @version $Id$
 *
 * This program can be distributed under the terms of the Apache License v2.0.
 * See the file LICENSE for details.
 **/

package dev.unexist.showcase.todo.infrastructure.persistence;

import dev.unexist.showcase.todo.domain.todo.DueDate;
import dev.unexist.showcase.todo.domain.todo.Todo;
import dev.unexist.showcase.todo.domain.todo.TodoRepository;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@QuarkusTestResource(value = HadoopResource.class, restrictToAnnotatedClass = true)
public class HadoopTodoRepositoryTest {

    @Inject
    TodoRepository repository;

    @Test
    public void shouldAddToAndGetFromRepository() {
        Todo todo_in = createTodo();

        assertThat(this.repository.add(todo_in)).isTrue();

        List<Todo> allTodos = this.repository.getAll();

        assertThat(allTodos).hasSize(1);
        assertThat(todo_in).isEqualTo(allTodos.get(0));
    }

    private static Todo createTodo() {
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