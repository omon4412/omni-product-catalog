package ru.bank.omniproductcatalog.producttype.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.bank.omniproductcatalog.producttype.repository.ProductType;

public interface ProductTypeService {
    Mono<ProductType> getProductTypeById(Long id);

    Mono<ProductType> getProductTypeByName(String name);

    Flux<ProductType> getAllProductTypes();
}
