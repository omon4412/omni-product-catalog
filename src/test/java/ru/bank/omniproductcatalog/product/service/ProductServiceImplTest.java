package ru.bank.omniproductcatalog.product.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
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
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {
    @InjectMocks
    private ProductServiceImpl productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ProductTypeService productTypeService;

    @Mock
    private CacheManager cacheManager;

    @Test
    void getAll4Products_withoutProductType_success() {
        List<Product> products = getProducts();
        List<ProductResponseDto> expected = getProductsResponse();

        when(productRepository.findAllOrderByOpenDate()).thenReturn(Flux.fromIterable(products));
        when(productMapper.toProductResponseDto(getCardProduct())).thenReturn(expected.get(0));
        when(productMapper.toProductResponseDto(getCreditProduct())).thenReturn(expected.get(1));
        when(productMapper.toProductResponseDto(getDepositProduct())).thenReturn(expected.get(2));
        when(productMapper.toProductResponseDto(getCardProduct_2())).thenReturn(expected.get(3));

        Flux<ProductResponseDto> result = productService.getAllProducts(null);

        verifyResult(result, expected);
    }

    @Test
    void getAll2Products_withProductType_success() {
        List<Product> products = getCardProducts();
        List<ProductResponseDto> expected = getCardProductsResponse();

        when(productRepository.findAllByTypeOrderByOpenDate("card")).thenReturn(Flux.fromIterable(products));
        when(productMapper.toProductResponseDto(getCardProduct())).thenReturn(expected.get(0));
        when(productMapper.toProductResponseDto(getCardProduct_2())).thenReturn(expected.get(1));

        Flux<ProductResponseDto> result = productService.getAllProducts("card");

        verifyResult2(result, expected);
    }

    @Test
    void getProductById_success() {
        List<Product> products = getCardProducts();
        ProductResponseDto expected = getCardProductResponse();

        when(productRepository.findProductById(1L)).thenReturn(Mono.just(products.get(0)));
        when(productMapper.toProductResponseDto(getCardProduct())).thenReturn(expected);

        Mono<ProductResponseDto> result = productService.getProductById(1L);

        StepVerifier.create(result)
                .expectNextMatches(p -> p.getId() != null && p.equals(expected))
                .verifyComplete();
    }

    @Test
    void getProductByIdNotFound_fail() {
        when(productRepository.findProductById(anyLong())).thenReturn(Mono.empty());

        Mono<ProductResponseDto> result = productService.getProductById(1L);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().equals("Product with id=" + 1L + " not found"))
                .verify();
    }

    @Test
    void deleteProductByIdNotFound_fail() {
        when(productRepository.findProductById(anyLong())).thenReturn(Mono.empty());

        Mono<Void> result = productService.deleteProduct(1L);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().equals("Product with id=" + 1L + " not found"))
                .verify();
    }

    @Test
    void deleteProductById_success() {
        Product product = getCardProduct();
        when(productRepository.findProductById(anyLong())).thenReturn(Mono.just(product));
        when(productRepository.deleteById(1L)).thenReturn(Mono.empty());

        Mono<Void> result = productService.deleteProduct(1L);

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void createProduct_success() {
        NewProductRequestDto request = new NewProductRequestDto("New card", 1L, "New Card Description", false);
        ProductResponseDto expected = getProductResponseDto(request);

        Mono<ProductResponseDto> result = productService.createProduct(request);

        StepVerifier.create(result)
                .expectNextMatches(p -> p.getId() != null && p.equals(expected))
                .verifyComplete();
    }

    @Test
    void updateProduct_whenProductNotFound_fail() {
        UpdateProductRequestDto request = new UpdateProductRequestDto("Update card", "Update Card Description", false);
        when(productRepository.findProductById(any())).thenReturn(Mono.empty());

        Mono<ProductResponseDto> result = productService.updateProduct(1L, request);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().contains("not found"))
                .verify();
    }

    @Test
    void updateProduct_whenProductIsAlreadyClosed_fail() {
        Product product = new Product();
        product.setId(1L);
        product.setOpenDate(Instant.now());
        product.setCloseDate(Instant.now());
        when(productRepository.findProductById(any())).thenReturn(Mono.just(product));

        UpdateProductRequestDto updateProductRequestDto = new UpdateProductRequestDto("Update card", "Update Card Description", true);

        Mono<ProductResponseDto> result = productService.updateProduct(1L, updateProductRequestDto);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof BadRequestException &&
                        throwable.getMessage().contains("Product is already closed"))
                .verify();
    }

    @Test
    void updateProduct_success() {
        UpdateProductRequestDto updateProductRequestDto = new UpdateProductRequestDto("Update card", "Update description", true);
        ProductResponseDto expected = new ProductResponseDto(1L, "Updated name", 1L, "Update description", null, null);
        Product product = new Product(1L, "Update card", 1L, "Updated Card Description", null, null, null, null, null, null, null);
        when(productRepository.findProductById(any())).thenReturn(Mono.just(product));
        when(productRepository.save(product)).thenReturn(Mono.just(product));
        when(productMapper.toProductResponseDto(product)).thenReturn(expected);
        Cache mockCache = Mockito.mock(Cache.class);
        when(cacheManager.getCache("products")).thenReturn(mockCache);
        doNothing().when(mockCache).put(anyLong(), any());
        doNothing().when(mockCache).evict(expected.getId());
        doNothing().when(mockCache).clear();

        Mono<ProductResponseDto> result = productService.updateProduct(1L, updateProductRequestDto);

        StepVerifier.create(result)
                .expectNextMatches(dto -> dto.getName().equals("Updated name") && dto.getDescription().equals("Update description"))
                .verifyComplete();
    }

    private static List<Product> getProducts() {
        List<Product> products = new ArrayList<>();
        products.add(getCardProduct());
        products.add(getCreditProduct());
        products.add(getDepositProduct());
        products.add(getCardProduct_2());
        return products;
    }

    private static List<Product> getCardProducts() {
        List<Product> products = new ArrayList<>();
        products.add(getCardProduct());
        products.add(getCardProduct_2());
        return products;
    }

    private static List<ProductResponseDto> getProductsResponse() {
        List<ProductResponseDto> products = new ArrayList<>();
        products.add(getCardProductResponse());
        products.add(getCreditProductResponse());
        products.add(getDepositProductResponse());
        products.add(getCardProductResponse_2());
        return products;
    }

    private static List<ProductResponseDto> getCardProductsResponse() {
        List<ProductResponseDto> products = new ArrayList<>();
        products.add(getCardProductResponse());
        products.add(getCardProductResponse_2());
        return products;
    }

    private static void verifyResult(Flux<ProductResponseDto> result,
                                     List<ProductResponseDto> expected) {
        StepVerifier.create(result)
                .expectNextMatches(p -> p.getId() != null && p.equals(expected.get(0)))
                .expectNextMatches(p -> p.getId() != null && p.equals(expected.get(1)))
                .expectNextMatches(p -> p.getId() != null && p.equals(expected.get(2)))
                .expectNextMatches(p -> p.getId() != null && p.equals(expected.get(3)))
                .verifyComplete();
    }

    private static void verifyResult2(Flux<ProductResponseDto> result,
                                      List<ProductResponseDto> expected) {
        StepVerifier.create(result)
                .expectNextMatches(p -> p.getId() != null && p.equals(expected.get(0)))
                .expectNextMatches(p -> p.getId() != null && p.equals(expected.get(1)))
                .verifyComplete();
    }

    private static ProductResponseDto getCardProductResponse() {
        ProductResponseDto expectedProduct = new ProductResponseDto();
        expectedProduct.setId(1L);
        expectedProduct.setName("Test Card");
        expectedProduct.setProductTypeId(1);
        expectedProduct.setDescription("Test Card Description");
        expectedProduct.setOpenDate(Instant.parse("2024-08-02T10:10:10.00Z"));
        return expectedProduct;
    }

    private static Product getCardProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Test Card");
        product.setProductTypeId(1L);
        product.setDescription("Test Card Description");
        product.setOpenDate(Instant.parse("2024-08-02T10:10:10.00Z"));
        return product;
    }

    private static ProductResponseDto getCardProductResponse_2() {
        ProductResponseDto expectedProduct = new ProductResponseDto();
        expectedProduct.setId(4L);
        expectedProduct.setName("Test Card 2");
        expectedProduct.setProductTypeId(1);
        expectedProduct.setDescription("Test Card 2 Description");
        expectedProduct.setOpenDate(Instant.parse("2024-08-02T10:10:10.00Z"));
        return expectedProduct;
    }

    private static Product getCardProduct_2() {
        Product product = new Product();
        product.setId(4L);
        product.setName("Test Card 2");
        product.setProductTypeId(1L);
        product.setDescription("Test Card 2 Description");
        product.setOpenDate(Instant.parse("2024-08-02T10:10:10.00Z"));
        return product;
    }

    private static ProductResponseDto getCreditProductResponse() {
        ProductResponseDto expectedProduct = new ProductResponseDto();
        expectedProduct.setId(2L);
        expectedProduct.setName("Test Credit");
        expectedProduct.setProductTypeId(2);
        expectedProduct.setDescription("Test Credit Description");
        expectedProduct.setOpenDate(Instant.parse("2024-08-02T10:10:10.00Z"));
        return expectedProduct;
    }

    private static Product getCreditProduct() {
        Product expectedProduct = new Product();
        expectedProduct.setId(2L);
        expectedProduct.setName("Test Credit");
        expectedProduct.setProductTypeId(2L);
        expectedProduct.setDescription("Test Credit Description");
        expectedProduct.setOpenDate(Instant.parse("2024-08-02T10:10:10.00Z"));
        return expectedProduct;
    }

    private static ProductResponseDto getDepositProductResponse() {
        ProductResponseDto expectedProduct = new ProductResponseDto();
        expectedProduct.setId(3L);
        expectedProduct.setName("Test Deposit");
        expectedProduct.setProductTypeId(3);
        expectedProduct.setDescription("Test Deposit Description");
        expectedProduct.setOpenDate(Instant.parse("2024-08-02T10:10:10.00Z"));
        return expectedProduct;
    }

    private static Product getDepositProduct() {
        Product expectedProduct = new Product();
        expectedProduct.setId(3L);
        expectedProduct.setName("Test Deposit");
        expectedProduct.setProductTypeId(3L);
        expectedProduct.setDescription("Test Deposit Description");
        expectedProduct.setOpenDate(Instant.parse("2024-08-02T10:10:10.00Z"));
        return expectedProduct;
    }

    private ProductResponseDto getProductResponseDto(NewProductRequestDto request) {
        ProductResponseDto expected = new ProductResponseDto(1L, "New card", 1L, "New Card Description", null, null);
        Product card = new Product(1L, "New card", 1L, "New Card Description", null, null, null, null, null, null, null);
        ProductType cardType = new ProductType(1L, "card", "description", null, null, null, null);
        when(productMapper.toProduct(request)).thenReturn(card);
        when(productTypeService.getProductTypeById(1L)).thenReturn(Mono.just(cardType));
        when(productRepository.save(card)).thenReturn(Mono.just(card));
        when(productMapper.toProductResponseDto(card)).thenReturn(expected);
        Cache mockCache = Mockito.mock(Cache.class);
        when(cacheManager.getCache("products")).thenReturn(mockCache);
        doNothing().when(mockCache).put(expected.getId(), expected);
        doNothing().when(mockCache).clear();
        return expected;
    }
}