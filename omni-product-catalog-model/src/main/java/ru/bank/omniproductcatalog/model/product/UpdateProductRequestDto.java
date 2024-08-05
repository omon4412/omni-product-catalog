package ru.bank.omniproductcatalog.model.product;

public record UpdateProductRequestDto(
        String name,
        String description,
        Boolean open
) {
}

