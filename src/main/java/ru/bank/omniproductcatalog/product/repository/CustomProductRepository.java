package ru.bank.omniproductcatalog.product.repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomProductRepository {
    Flux<Product> findAllByTypeOrderByOpenDate(String type);

    Flux<Product> findAllOrderByOpenDate();

    Mono<Product> findProductById(Long id);
}
