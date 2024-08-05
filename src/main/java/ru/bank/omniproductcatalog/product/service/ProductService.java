package ru.bank.omniproductcatalog.product.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.bank.omniproductcatalog.model.product.NewProductRequestDto;
import ru.bank.omniproductcatalog.model.product.ProductResponseDto;
import ru.bank.omniproductcatalog.model.product.UpdateProductRequestDto;

public interface ProductService {

    Mono<ProductResponseDto> createProduct(NewProductRequestDto newProductRequestDto);

    Flux<ProductResponseDto> getAllProducts(String type);

    Mono<ProductResponseDto> getProductById(Long id);

    Mono<Void> deleteProduct(Long id);

    Mono<ProductResponseDto> updateProduct(Long id, UpdateProductRequestDto updateProductRequestDto);
}
