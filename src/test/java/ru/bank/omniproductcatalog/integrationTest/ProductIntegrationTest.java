package ru.bank.omniproductcatalog.integrationTest;

import io.r2dbc.spi.ConnectionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.r2dbc.connection.init.ScriptUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import reactor.core.publisher.Mono;
import ru.bank.omniproductcatalog.model.product.NewProductRequestDto;
import ru.bank.omniproductcatalog.model.product.ProductResponseDto;
import ru.bank.omniproductcatalog.model.product.UpdateProductRequestDto;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestContainersConfig.class)
public class ProductIntegrationTest {
    @Autowired
    private WebTestClient webTestClient;


    @Autowired
    private ConnectionFactory connectionFactory;

    @BeforeEach
    void setUp(@Value("classpath:insert4product.sql") Resource sqlScript) {
        executeScriptBlocking(sqlScript);
    }

    @Test
    void getAll4Products_success() {
        ProductResponseDto expectedCardProduct = getCardProduct();
        ProductResponseDto expectedCreditProduct = getCreditProduct();
        ProductResponseDto expectedDepositProduct = getDepositProduct();
        ProductResponseDto expectedCardProduct2 = getCardProduct_2();

        List<ProductResponseDto> expectedProducts = Arrays.asList(expectedCardProduct,
                expectedCreditProduct, expectedDepositProduct, expectedCardProduct2);

        webTestClient.get().uri("/api/v1/products")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponseDto.class).value(response -> {
                    assertThat(response).hasSize(4);
                    assertThat(response).containsExactlyInAnyOrderElementsOf(expectedProducts);
                });
    }

    @Test
    void getAll2ProductsByType_success() {
        ProductResponseDto expectedCardProduct = getCardProduct();
        ProductResponseDto expectedCardProduct2 = getCardProduct_2();

        List<ProductResponseDto> expectedProducts = Arrays.asList(expectedCardProduct, expectedCardProduct2);

        webTestClient.get().uri("/api/v1/products?type=card")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponseDto.class).value(response -> {
                    assertThat(response).hasSize(2);
                    assertThat(response).containsExactlyInAnyOrderElementsOf(expectedProducts);
                });
    }

    @Test
    void getProductById_whenProductNotFound_fail() {
        webTestClient.get().uri("/api/v1/products/{id}", 999)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getProductById_success() {
        ProductResponseDto expectedCardProduct = getCardProduct();

        webTestClient.get().uri("/api/v1/products/{id}", 1)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponseDto.class).value(getProductConsumer(expectedCardProduct));
    }

    @Test
    void deleteProduct_success() {
        ProductResponseDto expectedCreditProduct = getCreditProduct();
        ProductResponseDto expectedDepositProduct = getDepositProduct();
        ProductResponseDto expectedCardProduct2 = getCardProduct_2();

        List<ProductResponseDto> expectedProducts = Arrays.asList(expectedCreditProduct, expectedDepositProduct, expectedCardProduct2);
        webTestClient.delete().uri("/api/v1/products/{id}", 1)
                .exchange()
                .expectStatus().isOk();
        webTestClient.get().uri("/api/v1/products")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponseDto.class).value(response -> {
                    assertThat(response).hasSize(3);
                    assertThat(response).containsExactlyInAnyOrderElementsOf(expectedProducts);
                });
    }

    @Test
    void deleteProduct_whenProductNotFound_fail() {
        webTestClient.delete().uri("/api/v1/products/{id}", 999)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void addProduct_success() {
        ProductResponseDto expectedProduct = new ProductResponseDto(5L, "New card", 1, "New Test Card Description",
                null, null);
        NewProductRequestDto newProductRequestDto = new NewProductRequestDto("New card", 1, "New Test Card Description", false);
        webTestClient.post().uri("/api/v1/products")
                .bodyValue(newProductRequestDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ProductResponseDto.class).value(getProductConsumer(expectedProduct));
        webTestClient.get().uri("/api/v1/products")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponseDto.class).value(response -> {
                    assertThat(response).hasSize(5);
                });
    }

    @Test
    void addProduct_whenProductTypeNotExists_fail() {
        NewProductRequestDto newProductRequestDto = new NewProductRequestDto("New card", 9999, "New Test Card Description", false);
        webTestClient.post().uri("/api/v1/products")
                .bodyValue(newProductRequestDto)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void updateProduct_success() {
        ProductResponseDto expectedProduct = new ProductResponseDto(1L, "Updated card", 1, "Updated Test Card Description",
                Instant.parse("2024-08-02T10:10:10.00Z"), null);
        UpdateProductRequestDto updateProductRequestDto = new UpdateProductRequestDto("Updated card", "Updated Test Card Description", null);
        webTestClient.put().uri("/api/v1/products/{id}", 1)
                .bodyValue(updateProductRequestDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponseDto.class).value(getProductConsumer(expectedProduct));
    }


    private static Consumer<ProductResponseDto> getProductConsumer(ProductResponseDto expectedProduct) {
        return response -> {
            assertThat(response.getId()).isEqualTo(expectedProduct.getId());
            assertThat(response.getName()).isEqualTo(expectedProduct.getName());
            assertThat(response.getDescription()).isEqualTo(expectedProduct.getDescription());
            assertThat(response.getCloseDate()).isEqualTo(expectedProduct.getCloseDate());
            assertThat(response.getOpenDate()).isEqualTo(expectedProduct.getOpenDate());
        };
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

    private void executeScriptBlocking(final Resource sqlScript) {
        Mono.from(connectionFactory.create()).flatMap(connection -> ScriptUtils.executeSqlScript(connection, sqlScript)).block();
    }
}
