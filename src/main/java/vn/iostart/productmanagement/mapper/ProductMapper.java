package vn.iostart.productmanagement.mapper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import vn.iostart.productmanagement.dto.ProductDTO;
import vn.iostart.productmanagement.dto.ProductImageDTO;
import vn.iostart.productmanagement.entity.Category;
import vn.iostart.productmanagement.entity.Product;

@Component
@RequiredArgsConstructor
public class ProductMapper {

    private final ProductImageMapper imageMapper;

    public ProductDTO toDTO(Product entity) {
        if (entity == null) {
            return null;
        }

        List<ProductImageDTO> imageDTOs =
                entity.getImages() == null
                        ? new ArrayList<>()
                        : entity.getImages()
                                .stream()
                                .map(imageMapper::toDTO)
                                .toList();

        Category category = entity.getCategory();

        return ProductDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .price(entity.getPrice())
                .quantity(entity.getQuantity())
                .description(entity.getDescription())
                .categoryId(
                        category == null
                                ? null
                                : category.getCategoryId()
                )
                .categoryName(
                        category == null
                                ? null
                                : category.getCategoryName()
                )
                .images(imageDTOs)
                .build();
    }

    public Product toEntity(ProductDTO dto) {
        if (dto == null) {
            return null;
        }

        return Product.builder()
                .id(dto.getId())
                .name(dto.getName())
                .price(dto.getPrice())
                .quantity(dto.getQuantity())
                .description(dto.getDescription())
                .build();
    }

    public Product toEntity(
            ProductDTO dto,
            Category category
    ) {
        Product product = toEntity(dto);
        product.setCategory(category);
        return product;
    }

    public void updateEntity(
            ProductDTO dto,
            Product entity
    ) {
        entity.setName(dto.getName());
        entity.setPrice(dto.getPrice());
        entity.setQuantity(dto.getQuantity());
        entity.setDescription(dto.getDescription());
    }

    public void updateEntity(
            ProductDTO dto,
            Product entity,
            Category category
    ) {
        updateEntity(dto, entity);
        entity.setCategory(category);
    }
}