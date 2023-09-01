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
    public void shouldCreateAndFetchMultipleFromRepository() {
        Todo todo = TodoFixture.createTodo();

        assertThat(this.repository.add(todo)).isTrue();
        assertThat(this.repository.add(todo)).isTrue();
        assertThat(this.repository.add(todo)).isTrue();

        List<Todo> allTodos = this.repository.getAll();

        assertThat(allTodos).hasSize(3);
        assertThat(todo).isEqualTo(allTodos.get(0))
                .isEqualTo(allTodos.get(1))
                .isEqualTo(allTodos.get(2));

    }
}