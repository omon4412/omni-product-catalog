package ru.bank.omniproductcatalog.model.product;

import java.time.Instant;
import java.util.Objects;

public class ProductResponseDto {
    private Long id;
    private String name;
    private long productTypeId;
    private String description;
    private Instant openDate;
    private Instant closeDate;

    public ProductResponseDto(Long id, String name, long productTypeId, String description, Instant openDate, Instant closeDate) {
        this.id = id;
        this.name = name;
        this.productTypeId = productTypeId;
        this.description = description;
        this.openDate = openDate;
        this.closeDate = closeDate;
    }

    public ProductResponseDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getProductTypeId() {
        return productTypeId;
    }

    public void setProductTypeId(long productTypeId) {
        this.productTypeId = productTypeId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getOpenDate() {
        return openDate;
    }

    public void setOpenDate(Instant openDate) {
        this.openDate = openDate;
    }

    public Instant getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(Instant closeDate) {
        this.closeDate = closeDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductResponseDto that = (ProductResponseDto) o;
        return productTypeId == that.productTypeId && Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(description, that.description) && Objects.equals(openDate, that.openDate) && Objects.equals(closeDate, that.closeDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, productTypeId, description, openDate, closeDate);
    }

    @Override
    public String toString() {
        return "ProductResponseDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", productTypeId=" + productTypeId +
                ", description='" + description + '\'' +
                ", openDate=" + openDate +
                ", closeDate=" + closeDate +
                '}';
    }
}
