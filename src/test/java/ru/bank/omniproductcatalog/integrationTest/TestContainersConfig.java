package ru.bank.omniproductcatalog.integrationTest;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@Configuration
public class TestContainersConfig {
    private static final PostgreSQLContainer<?> POSTGRES_CONTAINER =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("omni-product-catalog-test")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @BeforeAll
    public static void beforeAll() {
        POSTGRES_CONTAINER.start();
    }

    @AfterAll
    public static void afterAll() {
        POSTGRES_CONTAINER.stop();
    }

    @DynamicPropertySource
    public static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () -> "r2dbc:postgresql://" + POSTGRES_CONTAINER.getHost() + ":" + POSTGRES_CONTAINER.getFirstMappedPort() + "/omni-product-catalog-test");
        registry.add("spring.r2dbc.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.r2dbc.password", POSTGRES_CONTAINER::getPassword);
        registry.add("spring.liquibase.url", () -> "jdbc:postgresql://" + POSTGRES_CONTAINER.getHost() + ":" + POSTGRES_CONTAINER.getFirstMappedPort() + "/omni-product-catalog-test");
        registry.add("spring.liquibase.user", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.liquibase.password", POSTGRES_CONTAINER::getPassword);
    }
}
