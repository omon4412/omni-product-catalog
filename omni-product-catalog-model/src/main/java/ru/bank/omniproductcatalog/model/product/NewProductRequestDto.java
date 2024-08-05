package ru.bank.omniproductcatalog.model.product;

public record NewProductRequestDto(
        String name,
        long productTypeId,
        String description,
        boolean open
) {
}

