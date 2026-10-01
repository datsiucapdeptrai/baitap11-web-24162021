package vn.iostart.productmanagement.controller.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import vn.iostart.productmanagement.dto.ApiResponse;
import vn.iostart.productmanagement.dto.CategoryDTO;
import vn.iostart.productmanagement.entity.Category;
import vn.iostart.productmanagement.mapper.CategoryMapper;
import vn.iostart.productmanagement.repository.CategoryRepository;
import vn.iostart.productmanagement.service.CategoryIconStorageService;
import vn.iostart.productmanagement.service.CategoryService;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(
    name = "Category API",
    description = "REST API quản lý danh mục sản phẩm"
)
public class CategoryApiController {

    private final CategoryService categoryService;
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final CategoryIconStorageService categoryIconStorageService;

    @GetMapping
    @Operation(summary = "Lấy toàn bộ danh mục")
    public ResponseEntity<?> getAllCategories() {
        List<CategoryDTO> categories = categoryService.findAll()
                .stream()
                .map(categoryMapper::toDTO)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Lấy danh sách danh mục thành công",
                        categories
                )
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy danh mục theo ID")
    public ResponseEntity<?> getCategoryById(
            @PathVariable Long id
    ) {
        Category category = categoryService.findById(id)
                .orElse(null);

        if (category == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(
                            "Không tìm thấy danh mục có ID: " + id
                    ));
        }

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Tìm thấy danh mục",
                        categoryMapper.toDTO(category)
                )
        );
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm danh mục theo tên")
    public ResponseEntity<?> searchCategories(
            @RequestParam(required = false) String keyword
    ) {
        List<CategoryDTO> categories = categoryService.search(keyword)
                .stream()
                .map(categoryMapper::toDTO)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Tìm kiếm danh mục thành công",
                        categories
                )
        );
    }

    @PostMapping(
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(summary = "Thêm danh mục và upload icon")
    public ResponseEntity<?> createCategory(
            @RequestParam("categoryName") String categoryName,
            @RequestPart(
                value = "icon",
                required = false
            ) MultipartFile icon
    ) {
        String normalizedName =
                categoryName == null ? "" : categoryName.trim();

        if (normalizedName.isBlank()) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error(
                            "Tên danh mục không được để trống"
                    ));
        }

        if (categoryService.existsByCategoryName(normalizedName)) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(
                            "Tên danh mục đã tồn tại"
                    ));
        }

        try {
            String iconUrl =
                    categoryIconStorageService.store(icon);

            Category category = Category.builder()
                    .categoryName(normalizedName)
                    .icon(iconUrl)
                    .build();

            Category savedCategory =
                    categoryService.save(category);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.success(
                            "Thêm danh mục thành công",
                            categoryMapper.toDTO(savedCategory)
                    ));

        } catch (IllegalArgumentException exception) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error(
                            exception.getMessage()
                    ));
        }
    }

    @PutMapping(
        value = "/{id}",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(summary = "Cập nhật danh mục và icon")
    public ResponseEntity<?> updateCategory(
            @PathVariable Long id,
            @RequestParam("categoryName") String categoryName,
            @RequestPart(
                value = "icon",
                required = false
            ) MultipartFile icon
    ) {
        Category category = categoryService.findById(id)
                .orElse(null);

        if (category == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(
                            "Không tìm thấy danh mục có ID: " + id
                    ));
        }

        String normalizedName =
                categoryName == null ? "" : categoryName.trim();

        if (normalizedName.isBlank()) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error(
                            "Tên danh mục không được để trống"
                    ));
        }

        boolean duplicated = categoryRepository
                .existsByCategoryNameIgnoreCaseAndCategoryIdNot(
                        normalizedName,
                        id
                );

        if (duplicated) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(
                            "Tên danh mục đã được sử dụng"
                    ));
        }

        String oldIcon = category.getIcon();
        String newIcon = null;

        try {
            if (icon != null && !icon.isEmpty()) {
                newIcon = categoryIconStorageService.store(icon);
                category.setIcon(newIcon);
            }

            category.setCategoryName(normalizedName);

            Category updatedCategory =
                    categoryService.save(category);

            if (newIcon != null) {
                categoryIconStorageService.delete(oldIcon);
            }

            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Cập nhật danh mục thành công",
                            categoryMapper.toDTO(updatedCategory)
                    )
            );

        } catch (IllegalArgumentException exception) {
            if (newIcon != null) {
                categoryIconStorageService.delete(newIcon);
            }

            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error(
                            exception.getMessage()
                    ));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa danh mục")
    public ResponseEntity<?> deleteCategory(
            @PathVariable Long id
    ) {
        Category category = categoryService.findById(id)
                .orElse(null);

        if (category == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(
                            "Không tìm thấy danh mục có ID: " + id
                    ));
        }

        categoryService.deleteById(id);
        categoryIconStorageService.delete(category.getIcon());

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Xóa danh mục thành công",
                        null
                )
        );
    }
}