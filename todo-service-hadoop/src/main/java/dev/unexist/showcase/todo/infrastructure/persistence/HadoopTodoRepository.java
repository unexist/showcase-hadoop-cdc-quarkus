/**
 * @package Showcase-Hadoop-CDC-Quarkus
 *
 * @file Todo repository
 * @copyright 2023-present Christoph Kappel <christoph@unexist.dev>
 * @version $Id$
 *
 * This program can be distributed under the terms of the Apache License v2.0.
 * See the file LICENSE for details.
 **/

package dev.unexist.showcase.todo.infrastructure.persistence;

import dev.unexist.showcase.todo.domain.todo.Todo;
import dev.unexist.showcase.todo.domain.todo.TodoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.NotImplementedException;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class HadoopTodoRepository implements TodoRepository {

    @Override
    public boolean add(Todo todo) {
        throw new NotImplementedException("Needs to be implemented later");
    }

    @Override
    public boolean update(Todo todo) {
        throw new NotImplementedException("Needs to be implemented later");
    }

    @Override
    public boolean deleteById(int id) {
        throw new NotImplementedException("Needs to be implemented later");
    }

    @Override
    public List<Todo> getAll() {
        throw new NotImplementedException("Needs to be implemented later");
    }

    @Override
    public Optional<Todo> findById(int id) {
        throw new NotImplementedException("Needs to be implemented later");
    }
}
