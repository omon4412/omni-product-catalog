package ru.bank.omniproductcatalog.product.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.bank.omniproductcatalog.model.exception.BadRequestException;
import ru.bank.omniproductcatalog.model.exception.NotFoundException;
import ru.bank.omniproductcatalog.model.product.NewProductRequestDto;
import ru.bank.omniproductcatalog.model.product.ProductResponseDto;
import ru.bank.omniproductcatalog.model.product.UpdateProductRequestDto;
import ru.bank.omniproductcatalog.product.mapper.ProductMapper;
import ru.bank.omniproductcatalog.product.repository.Product;
import ru.bank.omniproductcatalog.product.repository.ProductRepository;
import ru.bank.omniproductcatalog.producttype.repository.ProductType;
import ru.bank.omniproductcatalog.producttype.service.ProductTypeService;

import java.time.Instant;

@Service
public class ProductServiceImpl implements ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ProductRepository productRepository;
    private final ProductTypeService productTypeService;
    private final ProductMapper productMapper;

    private final CacheManager cacheManager;

    public ProductServiceImpl(ProductRepository productRepository,
                              ProductTypeService productTypeService,
                              ProductMapper productMapper,
                              CacheManager cacheManager) {
        this.productRepository = productRepository;
        this.productTypeService = productTypeService;
        this.productMapper = productMapper;
        this.cacheManager = cacheManager;
    }


    @Override
    @Cacheable(value = "products")
    public Flux<ProductResponseDto> getAllProducts(String type) {
        if (StringUtils.hasText(type)) {
            return productRepository.findAllByTypeOrderByOpenDate(type)
                    .map(productMapper::toProductResponseDto);
        }
        return productRepository.findAllOrderByOpenDate()
                .map(productMapper::toProductResponseDto);
    }

    @Override
    @Cacheable(value = "products", key = "#id")
    public Mono<ProductResponseDto> getProductById(Long id) {
        return productRepository.findProductById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Product with id=" + id + " not found")))
                .doOnError(NotFoundException.class, e -> logger.error("Product with id={} not found", id))
                .map(productMapper::toProductResponseDto);
    }

    @Override
    public Mono<ProductResponseDto> createProduct(NewProductRequestDto newProductRequestDto) {
        return productTypeService.getProductTypeById(newProductRequestDto.productTypeId())
                .flatMap(productType -> {
                    Product product = createProductFromRequest(newProductRequestDto, productType);
                    return productRepository.save(product)
                            .map(savedProduct -> {
                                ProductResponseDto productResponseDto = mapToResponseDto(savedProduct, productType);
                                cacheManager.getCache("products").put(savedProduct.getId(), productResponseDto);
                                cacheManager.getCache("products").clear();
                                return productResponseDto;
                            });
                });
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "products", allEntries = true),
            @CacheEvict(value = "products", key = "#id")
    })
    public Mono<Void> deleteProduct(Long id) {
        return productRepository.findProductById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Product with id=" + id + " not found")))
                .flatMap(product -> productRepository.deleteById(id))
                .doOnError(NotFoundException.class, e -> logger.error("Product with id={} not found", id));
    }

    @Override
    public Mono<ProductResponseDto> updateProduct(Long id, UpdateProductRequestDto updateProductRequestDto) {
        return productRepository.findProductById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Product with id=" + id + " not found")))
                .flatMap(product -> {
                    if (updateProductRequestDto.name() != null)
                        product.setName(updateProductRequestDto.name());
                    if (updateProductRequestDto.description() != null)
                        product.setDescription(updateProductRequestDto.description());

                    if (updateProductRequestDto.open() != null) {
                        if (product.getOpenDate() == null && updateProductRequestDto.open()) {
                            product.setOpenDate(Instant.now());
                        } else if (product.getOpenDate() != null && !updateProductRequestDto.open()) {
                            product.setCloseDate(Instant.now());
                        } else if (product.getCloseDate() != null && updateProductRequestDto.open()) {
                            return Mono.error(new BadRequestException("Product is already closed"));
                        }
                    }
                    product.setLastModifyTime(Instant.now());
                    product.setProductType(null);
                    return productRepository.save(product);
                })
                .map(savedProduct -> {
                    ProductResponseDto productResponseDto = productMapper.toProductResponseDto(savedProduct);
                    cacheManager.getCache("products").evict(savedProduct.getId());
                    cacheManager.getCache("products").put(savedProduct.getId(), productResponseDto);
                    cacheManager.getCache("products").clear();
                    return productResponseDto;
                });
    }

    private Product createProductFromRequest(NewProductRequestDto newProductRequestDto, ProductType productType) {
        Product product = productMapper.toProduct(newProductRequestDto);
        Instant now = Instant.now();

        if (newProductRequestDto.open()) {
            product.setOpenDate(now);
        }

        product.setCreateTime(now);
        product.setLastModifyTime(now);
        product.setProductTypeId(productType.getId());
        product.setCreateUser("omni");
        product.setLastModifyUser("omni");

        return product;
    }

    private ProductResponseDto mapToResponseDto(Product product, ProductType productType) {
        product.setProductType(productType);
        return productMapper.toProductResponseDto(product);
    }
}
