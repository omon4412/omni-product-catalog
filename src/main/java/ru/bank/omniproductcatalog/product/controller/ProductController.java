package ru.bank.omniproductcatalog.product.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.bank.omniproductcatalog.model.exception.FieldValidationException;
import ru.bank.omniproductcatalog.model.exception.ValidationError;
import ru.bank.omniproductcatalog.model.product.NewProductRequestDto;
import ru.bank.omniproductcatalog.model.product.ProductResponseDto;
import ru.bank.omniproductcatalog.model.product.UpdateProductRequestDto;
import ru.bank.omniproductcatalog.product.service.ProductService;
import ru.bank.omniproductcatalog.util.MonoUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;
    private final MonoUtils monoUtils;
    private final Long timeout;

    public ProductController(ProductService productService,
                             MonoUtils monoUtils,
                             @Value("${application.product.timeout}") Long timeout) {
        this.productService = productService;
        this.monoUtils = monoUtils;
        this.timeout = timeout;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Flux<ProductResponseDto> getProducts(@RequestParam(required = false) String type) {
        logger.info("Fetching products with type: {}", type);
        return monoUtils.oksServiceCallableRight(productService.getAllProducts(type), timeout)
                .doOnNext(product -> {
                    if (type != null) {
                        logger.info("Successfully fetched products with type: {}", type);
                    } else {
                        logger.info("Successfully fetched all products");
                    }
                });
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<ProductResponseDto> getProductById(@PathVariable Long id) {
        logger.info("Fetching product with id: {}", id);
        return monoUtils.oksServiceCallableRight(productService.getProductById(id), timeout)
                .doOnNext(product -> logger.info("Successfully fetched product with id={}", id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ProductResponseDto> createProduct(@RequestBody NewProductRequestDto newProductRequestDto) {
        logger.info("Creating product: {}", newProductRequestDto);
        List<ValidationError> validationErrors = validateNewRequest(newProductRequestDto);

        if (!validationErrors.isEmpty()) {
            return Mono.error(new FieldValidationException("Invalid product data", validationErrors));
        }
        return monoUtils.oksServiceCallableRight(productService.createProduct(newProductRequestDto), timeout)
                .doOnNext(product -> logger.info("Successfully created product={}", newProductRequestDto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Void> deleteProduct(@PathVariable Long id) {
        logger.info("Deleting product with id: {}", id);
        return monoUtils.oksServiceCallableRight(productService.deleteProduct(id), timeout)
                .then(Mono.fromRunnable(() -> logger.info("Successfully deleted product with id={}", id)));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<ProductResponseDto> updateProduct(@RequestBody UpdateProductRequestDto updateProductRequestDto, @PathVariable Long id) {
        logger.info("Updating product: {}", updateProductRequestDto);
        List<ValidationError> validationErrors = validateUpdateRequest(updateProductRequestDto);
        if (!validationErrors.isEmpty()) {
            return Mono.error(new FieldValidationException("Invalid product data", validationErrors));
        }
        return monoUtils.oksServiceCallableRight(productService.updateProduct(id, updateProductRequestDto), timeout)
                .doOnNext(product -> logger.info("Successfully updated product={}", updateProductRequestDto));
    }

    public List<ValidationError> validateNewRequest(NewProductRequestDto newProductRequestDto) {
        List<ValidationError> errors = new ArrayList<>();
        Optional<NewProductRequestDto> optional = Optional.ofNullable(newProductRequestDto);

        if (optional.isEmpty()) {
            errors.add(new ValidationError("newProductRequestDto", "Product request cannot be null"));
            return errors;
        }

        validProductName(newProductRequestDto.name(), errors);
        validProductDescription(newProductRequestDto.description(), errors);

        return errors;
    }

    public List<ValidationError> validateUpdateRequest(UpdateProductRequestDto updateProductRequestDto) {
        List<ValidationError> errors = new ArrayList<>();
        Optional<UpdateProductRequestDto> optional = Optional.ofNullable(updateProductRequestDto);

        if (optional.isEmpty()) {
            errors.add(new ValidationError("updateProductRequestDto", "Product request cannot be null"));
            return errors;
        }

        if (StringUtils.hasText(updateProductRequestDto.name())) {
            validProductName(updateProductRequestDto.name(), errors);
        }
        if (StringUtils.hasText(updateProductRequestDto.description())) {
            validProductDescription(updateProductRequestDto.description(), errors);
        }

        return errors;
    }

    private static void validProductDescription(String description, List<ValidationError> errors) {
        if (!StringUtils.hasText(description)) {
            errors.add(new ValidationError("description", "Description cannot be null or empty"));
        } else if (description.length() < 3 || description.length() > 256) {
            errors.add(new ValidationError("description", "Description must be between 3 and 256 characters"));
        }
    }

    private static void validProductName(String name, List<ValidationError> errors) {
        if (!StringUtils.hasText(name)) {
            errors.add(new ValidationError("name", "Name cannot be null or empty"));
        } else if (name.length() < 3 || name.length() > 128) {
            errors.add(new ValidationError("name", "Name must be between 3 and 128 characters"));
        }
    }
}
