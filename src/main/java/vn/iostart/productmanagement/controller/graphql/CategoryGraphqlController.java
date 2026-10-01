package vn.iostart.productmanagement.controller.graphql;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.iostart.productmanagement.dto.CategoryDTO;
import vn.iostart.productmanagement.dto.graphql.CategoryInput;
import vn.iostart.productmanagement.dto.graphql.CategoryPageResponse;
import vn.iostart.productmanagement.entity.Category;
import vn.iostart.productmanagement.mapper.CategoryMapper;
import vn.iostart.productmanagement.service.CategoryService;

@Controller
@RequiredArgsConstructor
public class CategoryGraphqlController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @QueryMapping
    public List<CategoryDTO> categories() {
        return categoryService.findAll()
                .stream()
                .map(categoryMapper::toDTO)
                .toList();
    }

    @QueryMapping
    public CategoryDTO categoryById(
            @Argument("id") Long id
    ) {
        return categoryService.findById(id)
                .map(categoryMapper::toDTO)
                .orElse(null);
    }

    @QueryMapping
    public CategoryPageResponse categoryPage(
            @Argument("keyword") String keyword,
            @Argument("page") int page,
            @Argument("size") int size
    ) {
        Page<CategoryDTO> categoryPage =
                categoryService
                        .findAll(
                                keyword,
                                page,
                                size
                        )
                        .map(categoryMapper::toDTO);

        return CategoryPageResponse.from(categoryPage);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public CategoryDTO createCategory(
            @Valid
            @Argument("input") CategoryInput input
    ) {
        String categoryName =
                input.getCategoryName().trim();

        if (categoryService.existsByCategoryName(
                categoryName
        )) {
            throw new IllegalArgumentException(
                    "Tên danh mục đã tồn tại"
            );
        }

        Category category = Category.builder()
                .categoryName(categoryName)
                .icon(normalizeIcon(input.getIcon()))
                .build();

        Category saved =
                categoryService.save(category);

        return categoryMapper.toDTO(saved);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public CategoryDTO updateCategory(
            @Argument("id") Long id,
            @Valid
            @Argument("input") CategoryInput input
    ) {
        Category category = categoryService
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy danh mục có ID: "
                                        + id
                        )
                );

        String categoryName =
                input.getCategoryName().trim();

        boolean duplicated =
                categoryService
                        .existsByCategoryNameExceptId(
                                categoryName,
                                id
                        );

        if (duplicated) {
            throw new IllegalArgumentException(
                    "Tên danh mục đã được sử dụng"
            );
        }

        category.setCategoryName(categoryName);
        category.setIcon(
                normalizeIcon(input.getIcon())
        );

        Category updated =
                categoryService.save(category);

        return categoryMapper.toDTO(updated);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean deleteCategory(
            @Argument("id") Long id
    ) {
        Category category = categoryService
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy danh mục có ID: "
                                        + id
                        )
                );

        if (category.getProducts() != null &&
                !category.getProducts().isEmpty()) {

            throw new IllegalStateException(
                    "Không thể xóa danh mục đang có sản phẩm"
            );
        }

        categoryService.deleteById(id);

        return true;
    }

    private String normalizeIcon(String icon) {
        if (icon == null || icon.isBlank()) {
            return null;
        }

        return icon.trim();
    }
}