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
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Map;

public class HadoopTestResource implements QuarkusTestResourceLifecycleManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(HadoopTestResource.class);

    private File baseDir;
    private MiniDFSCluster cluster;

    @Override
    public Map<String, String> start() {
        Map<String, String> retVal = Collections.emptyMap();

        Configuration configuration = new Configuration();

        try {
            this.baseDir = Files.createTempDirectory("test_hdfs").toFile().getAbsoluteFile();

            configuration.set(MiniDFSCluster.HDFS_MINIDFS_BASEDIR, this.baseDir.getAbsolutePath());

            MiniDFSCluster.Builder builder = new MiniDFSCluster.Builder(configuration);

            this.cluster = builder.build();

            retVal = Map.of("hadoop.defaultFS",
                    String.format("hdfs://localhost:%d", this.cluster.getNameNodePort()));

            LOGGER.info(String.format("\n---\nCluster is ready\n URL = %s\nPath = %s\n---\n",
                    this.cluster.getHttpUri(0), this.cluster.getDataDirectory()));
        } catch (IOException e) {
            LOGGER.error("Cannot create mini cluster ", e);
        }

        return retVal;
    }

    @Override
    public void stop() {
        if (null != this.cluster) {
            this.cluster.shutdown();
        }

        if (null != this.baseDir) {
            try {
                FileUtils.deleteDirectory(this.baseDir);
            } catch (IOException e) {
                /* Do nothing */
            }
        }
    }
}
