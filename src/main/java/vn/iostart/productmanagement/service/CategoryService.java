package vn.iostart.productmanagement.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;

import vn.iostart.productmanagement.entity.Category;

public interface CategoryService {

    List<Category> findAll();

    List<Category> search(String keyword);

    Page<Category> findAll(
            String keyword,
            int page,
            int size
    );

    Optional<Category> findById(Long id);

    Optional<Category> findByCategoryName(
            String categoryName
    );

    boolean existsByCategoryName(
            String categoryName
    );

    boolean existsByCategoryNameExceptId(
            String categoryName,
            Long categoryId
    );

    Category save(Category category);

    void deleteById(Long id);
}