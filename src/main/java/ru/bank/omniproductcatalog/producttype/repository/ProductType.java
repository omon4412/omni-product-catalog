package ru.bank.omniproductcatalog.producttype.repository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("product_type")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductType {
    @Id
    @Column("product_type_id")
    private Long id;
    private String name;
    private String description;
    @Column("create_time")
    private Instant createTime;
    @Column("create_user")
    private String createUser;
    @Column("last_modify_time")
    private Instant lastModifyTime;
    @Column("last_modify_user")
    private String lastModifyUser;
}
