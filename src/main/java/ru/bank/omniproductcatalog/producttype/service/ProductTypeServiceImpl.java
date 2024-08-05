package ru.bank.omniproductcatalog.producttype.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.bank.omniproductcatalog.model.exception.NotFoundException;
import ru.bank.omniproductcatalog.producttype.repository.ProductType;
import ru.bank.omniproductcatalog.producttype.repository.ProductTypeRepository;

@Service
public class ProductTypeServiceImpl implements ProductTypeService {
    private final ProductTypeRepository productTypeRepository;

    public ProductTypeServiceImpl(ProductTypeRepository productTypeRepository) {
        this.productTypeRepository = productTypeRepository;
    }

    @Override
    @Cacheable(value = "productTypes", key = "#id")
    public Mono<ProductType> getProductTypeById(Long id) {
        return productTypeRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Product type with id " + id + " not found")));
    }

    @Override
    @Cacheable(value = "productTypes", key = "#name")
    public Mono<ProductType> getProductTypeByName(String name) {
        return productTypeRepository.findByName(name)
                .switchIfEmpty(Mono.error(new NotFoundException("Product type with name " + name + " not found")));
    }

    @Override
    @Cacheable("productTypes")
    public Flux<ProductType> getAllProductTypes() {
        return productTypeRepository.findAll();
    }
}
