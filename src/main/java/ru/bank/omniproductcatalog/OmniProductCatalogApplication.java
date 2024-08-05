package ru.bank.omniproductcatalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import reactor.tools.agent.ReactorDebugAgent;

@SpringBootApplication
@EnableCaching
public class OmniProductCatalogApplication {

    public static void main(String[] args) {
        ReactorDebugAgent.init();
        SpringApplication.run(OmniProductCatalogApplication.class, args);
    }

}
