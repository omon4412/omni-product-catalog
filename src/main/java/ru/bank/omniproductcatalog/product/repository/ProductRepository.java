package ru.bank.omniproductcatalog.product.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ProductRepository extends ReactiveCrudRepository<Product, Long>, CustomProductRepository {
}
