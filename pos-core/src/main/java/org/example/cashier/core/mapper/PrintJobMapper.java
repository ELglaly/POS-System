package org.example.cashier.core.mapper;

import org.example.cashier.core.dto.PrintJobDTO;
import org.example.cashier.core.entity.PrintJob;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper — PrintJob entity <-> PrintJobDTO.
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PrintJobMapper {

    @Mapping(target = "productId",   source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productSku",  source = "product.sku")
    PrintJobDTO toDto(PrintJob printJob);

    List<PrintJobDTO> toDtoList(List<PrintJob> printJobs);
}
