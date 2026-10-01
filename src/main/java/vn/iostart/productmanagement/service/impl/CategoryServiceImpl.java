package vn.iostart.productmanagement.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import vn.iostart.productmanagement.entity.Category;
import vn.iostart.productmanagement.repository.CategoryRepository;
import vn.iostart.productmanagement.service.CategoryService;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl
        implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAll(
                Sort.by("categoryId").descending()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> search(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return categoryRepository.findAll(
                    Sort.by("categoryId").descending()
            );
        }

        return categoryRepository
                .findByCategoryNameContainingIgnoreCase(
                        keyword.trim()
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Category> findAll(
            String keyword,
            int page,
            int size
    ) {
        if (page < 0) {
            page = 0;
        }

        if (size <= 0) {
            size = 5;
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("categoryId").descending()
        );

        if (!StringUtils.hasText(keyword)) {
            return categoryRepository.findAll(pageable);
        }

        return categoryRepository
                .findByCategoryNameContainingIgnoreCase(
                        keyword.trim(),
                        pageable
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Category> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        return categoryRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Category> findByCategoryName(
            String categoryName
    ) {
        if (!StringUtils.hasText(categoryName)) {
            return Optional.empty();
        }

        return categoryRepository
                .findByCategoryNameIgnoreCase(
                        categoryName.trim()
                );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCategoryName(
            String categoryName
    ) {
        if (!StringUtils.hasText(categoryName)) {
            return false;
        }

        return categoryRepository
                .existsByCategoryNameIgnoreCase(
                        categoryName.trim()
                );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCategoryNameExceptId(
            String categoryName,
            Long categoryId
    ) {
        if (!StringUtils.hasText(categoryName) ||
                categoryId == null) {
            return false;
        }

        return categoryRepository
                .existsByCategoryNameIgnoreCaseAndCategoryIdNot(
                        categoryName.trim(),
                        categoryId
                );
    }

    @Override
    public Category save(Category category) {
        if (category == null ||
                !StringUtils.hasText(
                        category.getCategoryName()
                )) {
            throw new IllegalArgumentException(
                    "Tên danh mục không được để trống"
            );
        }

        category.setCategoryName(
                category.getCategoryName().trim()
        );

        return categoryRepository.save(category);
    }

    @Override
    public void deleteById(Long id) {
        Category category = categoryRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy danh mục có ID: "
                                        + id
                        )
                );

        categoryRepository.delete(category);
    }
}