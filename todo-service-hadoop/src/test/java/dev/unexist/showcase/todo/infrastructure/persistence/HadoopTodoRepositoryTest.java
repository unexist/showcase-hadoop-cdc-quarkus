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

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(value = HadoopResource.class, restrictToAnnotatedClass = true)
public class HadoopTodoRepositoryTest {

    @BeforeAll
    public void setUp() {

    }

    @Test
    public void shouldGetEmptytestTodoEndpoint() {
    }
}