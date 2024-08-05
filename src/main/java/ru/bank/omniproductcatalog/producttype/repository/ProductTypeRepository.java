package ru.bank.omniproductcatalog.producttype.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ProductTypeRepository extends ReactiveCrudRepository<ProductType, Long> {
    Mono<ProductType> findByName(String name);
}
