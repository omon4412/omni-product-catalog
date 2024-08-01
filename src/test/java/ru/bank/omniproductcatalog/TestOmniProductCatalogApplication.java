package ru.bank.omniproductcatalog;

import org.springframework.boot.SpringApplication;

public class TestOmniProductCatalogApplication {

    public static void main(String[] args) {
        SpringApplication.from(OmniProductCatalogApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
