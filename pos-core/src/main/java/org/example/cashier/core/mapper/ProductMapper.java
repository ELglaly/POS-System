package org.example.cashier.core.mapper;

import org.example.cashier.core.dto.ProductDTO;
import org.example.cashier.core.entity.Product;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper — Product entity <-> ProductDTO.
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {

    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "categoryCode", source = "category.code")
    @Mapping(target = "stockStatus",  expression = "java(product.getStockStatus())")
    ProductDTO toDto(Product product);

    List<ProductDTO> toDtoList(List<Product> products);
}
