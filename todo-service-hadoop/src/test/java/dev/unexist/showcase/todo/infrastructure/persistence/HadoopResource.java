/**
 * @package Showcase-Hadoop-CDC-Quarkus
 *
 * @file Hadoop test lifecycle manager
 * @copyright 2023-present Christoph Kappel <christoph@unexist.dev>
 * @version $Id$
 *
 * This program can be distributed under the terms of the Apache License v2.0.
 * See the file LICENSE for details.
 **/

package dev.unexist.showcase.todo.infrastructure.persistence;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.util.Map;

public class HadoopResource implements QuarkusTestResourceLifecycleManager {

    @Override
    public Map<String, String> start() {
        return null;
    }

    @Override
    public void stop() {

    }
}
