package ru.bank.omniproductcatalog.product.repository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import ru.bank.omniproductcatalog.producttype.repository.ProductType;

import java.time.Instant;

@Data
@Table("product")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    @Id
    @Column("product_id")
    private Long id;
    private String name;
    @Column("product_type_id")
    private Long productTypeId;
    private String description;
    private Instant openDate;
    private Instant closeDate;
    @Column("create_time")
    private Instant createTime;
    @Column("create_user")
    private String createUser;
    @Column("last_modify_time")
    private Instant lastModifyTime;
    @Column("last_modify_user")
    private String lastModifyUser;
    @Transient
    private ProductType productType;
}
