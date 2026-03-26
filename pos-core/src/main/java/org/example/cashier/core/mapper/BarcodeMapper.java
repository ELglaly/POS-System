package org.example.cashier.core.mapper;

import org.example.cashier.core.dto.BarcodeDTO;
import org.example.cashier.core.entity.Barcode;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper — Barcode entity <-> BarcodeDTO.
 *
 * componentModel = "spring" → MapStruct generates a Spring @Component so the
 * mapper is injectable anywhere in the Spring context.
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BarcodeMapper {

    @Mapping(target = "productId",   source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productSku",  source = "product.sku")
    @Mapping(target = "renderedImagePng", ignore = true)
    BarcodeDTO toDto(Barcode barcode);

    List<BarcodeDTO> toDtoList(List<Barcode> barcodes);

    /**
     * Partial update — apply non-null DTO fields onto an existing entity.
     * Used in PUT/PATCH operations.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "product",     ignore = true)
    @Mapping(target = "generatedAt", ignore = true)
    void updateEntityFromDto(BarcodeDTO dto, @MappingTarget Barcode entity);
}
