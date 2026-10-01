package vn.iostart.productmanagement.mapper;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import vn.iostart.productmanagement.dto.CategoryDTO;
import vn.iostart.productmanagement.entity.Category;

@Component
public class CategoryMapper {

    public CategoryDTO toDTO(Category category) {
        if (category == null) {
            return null;
        }

        return CategoryDTO.builder()
                .categoryId(category.getCategoryId())
                .categoryName(category.getCategoryName())
                .icon(category.getIcon())
                .build();
    }

    public Category toEntity(CategoryDTO dto) {
        if (dto == null) {
            return null;
        }

        return Category.builder()
                .categoryId(dto.getCategoryId())
                .categoryName(dto.getCategoryName())
                .icon(dto.getIcon())
                .build();
    }

    public void updateEntity(
            Category category,
            CategoryDTO dto
    ) {
        category.setCategoryName(dto.getCategoryName());

        if (StringUtils.hasText(dto.getIcon())) {
            category.setIcon(dto.getIcon());
        }
    }
}