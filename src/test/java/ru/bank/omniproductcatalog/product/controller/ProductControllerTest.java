package ru.bank.omniproductcatalog.product.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.bank.omniproductcatalog.exception.ErrorHandler;
import ru.bank.omniproductcatalog.model.product.NewProductRequestDto;
import ru.bank.omniproductcatalog.model.product.ProductResponseDto;
import ru.bank.omniproductcatalog.model.product.UpdateProductRequestDto;
import ru.bank.omniproductcatalog.product.service.ProductService;
import ru.bank.omniproductcatalog.util.MonoUtils;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @InjectMocks
    private ProductController productController;

    @Mock
    private ProductService productService;

    @Mock
    private MonoUtils monoUtils;

    private WebTestClient webTestClient;

    @BeforeEach
    void setup() {
        productController = new ProductController(productService, monoUtils, 5000L);
        webTestClient = WebTestClient.bindToController(productController).controllerAdvice(new ErrorHandler()).build();

    }

    @Test
    void getProductsWithoutType_success() {
        ProductResponseDto cardProduct = getCardProduct();
        ProductResponseDto creditProduct = getCreditProduct();
        ProductResponseDto depositProduct = getDepositProduct();
        ProductResponseDto cardProduct2 = getCardProduct_2();

        Flux<ProductResponseDto> expectedProducts = Flux.just(cardProduct,
                creditProduct, depositProduct, cardProduct2);
        when(monoUtils.oksServiceCallableRight(any(Flux.class), anyLong())).thenReturn(expectedProducts);
        when(productService.getAllProducts(null)).thenReturn(expectedProducts);

        webTestClient.get()
                .uri("/api/v1/products")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponseDto.class)
                .hasSize(4)
                .contains(cardProduct, creditProduct, depositProduct, cardProduct2);
    }

    @Test
    void getProductsWithType_success() {
        ProductResponseDto cardProduct = getCardProduct();
        ProductResponseDto cardProduct2 = getCardProduct_2();

        Flux<ProductResponseDto> expectedProducts = Flux.just(cardProduct, cardProduct2);
        when(monoUtils.oksServiceCallableRight(any(Flux.class), anyLong())).thenReturn(expectedProducts);
        when(productService.getAllProducts("card")).thenReturn(expectedProducts);

        webTestClient.get()
                .uri("/api/v1/products?type=card")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponseDto.class)
                .hasSize(2)
                .contains(cardProduct, cardProduct2);
    }

    @Test
    void createProduct_success() {
        NewProductRequestDto newProductRequestDto = new NewProductRequestDto("Product Name", 1L, "Product Description", false);
        ProductResponseDto expectedProductResponseDto = new ProductResponseDto(1L, "Product Name", 1L, "Product Description", null, null);

        when(productService.createProduct(eq(newProductRequestDto))).thenReturn(Mono.just(expectedProductResponseDto));
        when(monoUtils.oksServiceCallableRight(any(Mono.class), anyLong())).thenReturn(Mono.just(expectedProductResponseDto));

        webTestClient.post()
                .uri("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newProductRequestDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ProductResponseDto.class)
                .isEqualTo(expectedProductResponseDto);
    }

    @Test
    void createProduct_whenNameIsNull_success() {
        NewProductRequestDto newProductRequestDto = new NewProductRequestDto(null, 1L, "Product Description", false);

        webTestClient.post()
                .uri("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newProductRequestDto)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("[ValidationError[field=name, message=Name cannot be null or empty]]");
    }

    @Test
    void createProduct_whenNameIsBlank_success() {
        NewProductRequestDto newProductRequestDto = new NewProductRequestDto("      ", 1L, "Product Description", false);

        webTestClient.post()
                .uri("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newProductRequestDto)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("[ValidationError[field=name, message=Name cannot be null or empty]]");
    }

    @Test
    void createProduct_whenNameLengthLessThen3_success() {
        NewProductRequestDto newProductRequestDto = new NewProductRequestDto("te", 1L, "Product Description", false);

        webTestClient.post()
                .uri("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newProductRequestDto)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("[ValidationError[field=name, message=Name must be between 3 and 128 characters]]");
    }

    @Test
    void createProduct_whenNameLengthMoreThen128_success() {
        NewProductRequestDto newProductRequestDto
                = new NewProductRequestDto("tetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetete",
                1L, "Product Description", false);

        webTestClient.post()
                .uri("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newProductRequestDto)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("[ValidationError[field=name, message=Name must be between 3 and 128 characters]]");
    }

    @Test
    void createProduct_whenDescriptionIsNull_success() {
        NewProductRequestDto newProductRequestDto = new NewProductRequestDto("Card name", 1L, null, false);

        webTestClient.post()
                .uri("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newProductRequestDto)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("[ValidationError[field=description, message=Description cannot be null or empty]]");
    }

    @Test
    void createProduct_whenDescriptionIsBlank_success() {
        NewProductRequestDto newProductRequestDto = new NewProductRequestDto("Card name", 1L, "    ", false);

        webTestClient.post()
                .uri("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newProductRequestDto)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("[ValidationError[field=description, message=Description cannot be null or empty]]");
    }

    @Test
    void createProduct_whenDescriptionLengthLessThen3_success() {
        NewProductRequestDto newProductRequestDto = new NewProductRequestDto("Card name", 1L, "te", false);

        webTestClient.post()
                .uri("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newProductRequestDto)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("[ValidationError[field=description, message=Description must be between 3 and 256 characters]]");
    }

    @Test
    void createProduct_whenDescriptionLengthMoreThen128_success() {
        NewProductRequestDto newProductRequestDto
                = new NewProductRequestDto("Card name",
                1L, "tetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetete" +
                "tetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetetete", false);

        webTestClient.post()
                .uri("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newProductRequestDto)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("[ValidationError[field=description, message=Description must be between 3 and 256 characters]]");
    }

    @Test
    void updateProduct_success() {
        UpdateProductRequestDto updateProductRequestDto = new UpdateProductRequestDto("Product Name", "Product Description", false);
        ProductResponseDto expectedProductResponseDto = new ProductResponseDto(1L, "Product Name", 1L, "Product Description", null, null);

        when(productService.updateProduct(1L, updateProductRequestDto))
                .thenReturn(Mono.just(expectedProductResponseDto));
        when(monoUtils.oksServiceCallableRight(any(Mono.class), anyLong()))
                .thenReturn(Mono.just(expectedProductResponseDto));

        webTestClient.put()
                .uri("/api/v1/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateProductRequestDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponseDto.class)
                .isEqualTo(expectedProductResponseDto);
    }

    private static ProductResponseDto getCardProduct() {
        ProductResponseDto expectedProduct = new ProductResponseDto();
        expectedProduct.setId(1L);
        expectedProduct.setName("Test Card");
        expectedProduct.setProductTypeId(1);
        expectedProduct.setDescription("Test Card Description");
        expectedProduct.setOpenDate(Instant.parse("2024-08-02T10:10:10.00Z"));
        return expectedProduct;
    }

    private static ProductResponseDto getCardProduct_2() {
        ProductResponseDto expectedProduct = new ProductResponseDto();
        expectedProduct.setId(4L);
        expectedProduct.setName("Test Card 2");
        expectedProduct.setProductTypeId(1);
        expectedProduct.setDescription("Test Card 2 Description");
        expectedProduct.setOpenDate(Instant.parse("2024-08-02T10:10:10.00Z"));
        return expectedProduct;
    }

    private static ProductResponseDto getCreditProduct() {
        ProductResponseDto expectedProduct = new ProductResponseDto();
        expectedProduct.setId(2L);
        expectedProduct.setName("Test Credit");
        expectedProduct.setProductTypeId(2);
        expectedProduct.setDescription("Test Credit Description");
        expectedProduct.setOpenDate(Instant.parse("2024-08-02T10:10:10.00Z"));
        return expectedProduct;
    }

    private static ProductResponseDto getDepositProduct() {
        ProductResponseDto expectedProduct = new ProductResponseDto();
        expectedProduct.setId(3L);
        expectedProduct.setName("Test Deposit");
        expectedProduct.setProductTypeId(3);
        expectedProduct.setDescription("Test Deposit Description");
        expectedProduct.setOpenDate(Instant.parse("2024-08-02T10:10:10.00Z"));
        return expectedProduct;
    }
}







