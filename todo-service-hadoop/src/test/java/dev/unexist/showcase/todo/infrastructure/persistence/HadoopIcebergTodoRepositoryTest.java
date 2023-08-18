/**
 * @package Showcase-Hadoop-CDC-Quarkus
 *
 * @file Hadoop iceberg repository test
 * @copyright 2023-present Christoph Kappel <christoph@unexist.dev>
 * @version $Id$
 *
 * This program can be distributed under the terms of the Apache License v2.0.
 * See the file LICENSE for details.
 **/

package dev.unexist.showcase.todo.infrastructure.persistence;

import dev.unexist.showcase.todo.domain.todo.Todo;
import dev.unexist.showcase.todo.domain.todo.TodoRepository;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@QuarkusTestResource(value = HadoopTestResource.class, restrictToAnnotatedClass = true)
public class HadoopIcebergTodoRepositoryTest {

    @Inject
    @Named("hadoop_iceberg")
    TodoRepository repository;

    @Test
    public void shouldAddToAndGetFromRepository() {
        Todo todo_in = TodoFixture.createTodo();

        assertThat(this.repository.add(todo_in)).isTrue();

        List<Todo> allTodos = this.repository.getAll();

        assertThat(allTodos).hasSize(1);
        assertThat(todo_in).isEqualTo(allTodos.get(0));
    }
}