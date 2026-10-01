package vn.iostart.productmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.iostart.productmanagement.entity.ProductImage;

public interface ProductImageRepository
        extends JpaRepository<ProductImage, Long> {
}