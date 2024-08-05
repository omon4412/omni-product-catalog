package ru.bank.omniproductcatalog.product.repository;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.bank.omniproductcatalog.producttype.repository.ProductType;

import java.time.Instant;

@Repository
public class CustomProductRepositoryImpl implements CustomProductRepository {
    private final DatabaseClient client;

    public CustomProductRepositoryImpl(DatabaseClient client) {
        this.client = client;
    }

    @Override
    public Flux<Product> findAllByTypeOrderByOpenDate(String type) {
        String query = "SELECT p.product_id as p_id, p.name as p_name, p.product_type_id as p_product_type_id, " +
                "p.description as p_description, p.open_date as p_open_date, p.close_date as p_close_date, " +
                "p.create_time as p_create_time, p.create_user as p_create_user, p.last_modify_time as p_last_modify_time, " +
                "p.last_modify_user as p_last_modify_user, " +
                "pt.product_type_id as pt_id, pt.name as pt_name, pt.description as pt_description, " +
                "pt.create_time as pt_create_time, pt.create_user as pt_create_user, " +
                "pt.last_modify_time as pt_last_modify_time, pt.last_modify_user as pt_last_modify_user " +
                "FROM product p " +
                "LEFT JOIN product_type pt ON p.product_type_id = pt.product_type_id " +
                "WHERE pt.name=:type " +
                "ORDER BY p.open_date DESC NULLS LAST";

        return client.sql(query)
                .bind("type", type)
                .map(this::mapRowToProduct)
                .all();
    }

    @Override
    public Flux<Product> findAllOrderByOpenDate() {
        String query = "SELECT p.product_id as p_id, p.name as p_name, p.product_type_id as p_product_type_id, " +
                "p.description as p_description, p.open_date as p_open_date, p.close_date as p_close_date, " +
                "p.create_time as p_create_time, p.create_user as p_create_user, p.last_modify_time as p_last_modify_time, " +
                "p.last_modify_user as p_last_modify_user, " +
                "pt.product_type_id as pt_id, pt.name as pt_name, pt.description as pt_description, " +
                "pt.create_time as pt_create_time, pt.create_user as pt_create_user, " +
                "pt.last_modify_time as pt_last_modify_time, pt.last_modify_user as pt_last_modify_user " +
                "FROM product p " +
                "LEFT JOIN product_type pt ON p.product_type_id = pt.product_type_id " +
                "ORDER BY p.open_date DESC NULLS LAST";

        return client.sql(query)
                .map(this::mapRowToProduct)
                .all();
    }

    @Override
    public Mono<Product> findProductById(Long id) {
        String query = "SELECT p.product_id as p_id, p.name as p_name, p.product_type_id as p_product_type_id, " +
                "p.description as p_description, p.open_date as p_open_date, p.close_date as p_close_date, " +
                "p.create_time as p_create_time, p.create_user as p_create_user, p.last_modify_time as p_last_modify_time, " +
                "p.last_modify_user as p_last_modify_user, " +
                "pt.product_type_id as pt_id, pt.name as pt_name, pt.description as pt_description, " +
                "pt.create_time as pt_create_time, pt.create_user as pt_create_user, " +
                "pt.last_modify_time as pt_last_modify_time, pt.last_modify_user as pt_last_modify_user " +
                "FROM product p " +
                "LEFT JOIN product_type pt ON p.product_type_id = pt.product_type_id " +
                "WHERE p.product_id=:id";

        return client.sql(query)
                .bind("id", id)
                .map(this::mapRowToProduct)
                .one();
    }

    private Product mapRowToProduct(Row row, RowMetadata metadata) {
        ProductType productType = ProductType.builder()
                .id(row.get("pt_id", Long.class))
                .name(row.get("pt_name", String.class))
                .description(row.get("pt_description", String.class))
                .createTime(row.get("pt_create_time", Instant.class))
                .createUser(row.get("pt_create_user", String.class))
                .lastModifyTime(row.get("pt_last_modify_time", Instant.class))
                .lastModifyUser(row.get("pt_last_modify_user", String.class))
                .build();
        return Product.builder()
                .id(row.get("p_id", Long.class))
                .name(row.get("p_name", String.class))
                .productTypeId(row.get("p_product_type_id", Long.class))
                .description(row.get("p_description", String.class))
                .openDate(row.get("p_open_date", Instant.class))
                .closeDate(row.get("p_close_date", Instant.class))
                .createTime(row.get("p_create_time", Instant.class))
                .createUser(row.get("p_create_user", String.class))
                .lastModifyTime(row.get("p_last_modify_time", Instant.class))
                .lastModifyUser(row.get("p_last_modify_user", String.class))
                .productType(productType)
                .build();
    }
}
