package com.thevirtualforge.musicalog.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;

@TestConfiguration
public class TestMongoContainerConfiguration {

    @Container
    static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest")
        .withExposedPorts(27017);

    static {
        mongoDBContainer.start();
        var mappedPort = mongoDBContainer.getMappedPort(27017);
        System.setProperty("mongodb.container.port", String.valueOf(mappedPort));
    }
}
