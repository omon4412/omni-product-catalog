package ru.bank.omniproductcatalog.product.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import ru.bank.omniproductcatalog.model.product.NewProductRequestDto;
import ru.bank.omniproductcatalog.model.product.ProductResponseDto;
import ru.bank.omniproductcatalog.product.repository.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mappings({
            @Mapping(target = "id", source = "id"),
            @Mapping(target = "name", source = "name"),
            @Mapping(target = "productTypeId", source = "product.productTypeId"),
            @Mapping(target = "description", source = "description"),
            @Mapping(target = "openDate", source = "openDate"),
            @Mapping(target = "closeDate", source = "closeDate")
    })
    ProductResponseDto toProductResponseDto(Product product);

    @Mappings({
            @Mapping(target = "name", source = "name"),
            @Mapping(target = "description", source = "description"),
            @Mapping(target = "productTypeId", source = "productTypeId")
    })
    Product toProduct(NewProductRequestDto newProductRequestDto);
}
