package vn.iostart.productmanagement.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import vn.iostart.productmanagement.entity.Product;

public interface ProductRepository
        extends JpaRepository<Product, Long> {

    Page<Product> findByNameContainingIgnoreCase(
            String name,
            Pageable pageable
    );

    List<Product> findAllByOrderByPriceAsc();

    List<Product> findByCategoryCategoryIdOrderByPriceAsc(
            Long categoryId
    );

    Page<Product> findByCategoryCategoryId(
            Long categoryId,
            Pageable pageable
    );

    Page<Product>
    findByCategoryCategoryIdAndNameContainingIgnoreCase(
            Long categoryId,
            String keyword,
            Pageable pageable
    );
}