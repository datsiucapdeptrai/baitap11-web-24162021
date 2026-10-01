package vn.iostart.productmanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.iostart.productmanagement.entity.Category;

@Repository
public interface CategoryRepository
        extends JpaRepository<Category, Long> {

    Optional<Category> findByCategoryNameIgnoreCase(
            String categoryName
    );

    boolean existsByCategoryNameIgnoreCase(
            String categoryName
    );

    boolean existsByCategoryNameIgnoreCaseAndCategoryIdNot(
            String categoryName,
            Long categoryId
    );

    List<Category> findByCategoryNameContainingIgnoreCase(
            String keyword
    );

    Page<Category> findByCategoryNameContainingIgnoreCase(
            String keyword,
            Pageable pageable
    );
}