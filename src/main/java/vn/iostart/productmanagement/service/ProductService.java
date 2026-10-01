package vn.iostart.productmanagement.service;

import java.util.List;

import org.springframework.data.domain.Page;

import vn.iostart.productmanagement.dto.ProductDTO;

public interface ProductService {

    Page<ProductDTO> findAll(
            String keyword,
            int page,
            int size
    );

    Page<ProductDTO> search(
            String keyword,
            Long categoryId,
            int page,
            int size
    );

    List<ProductDTO> findAllByPriceAsc();

    List<ProductDTO> findByCategory(
            Long categoryId
    );

    ProductDTO findById(Long id);

    ProductDTO create(ProductDTO dto);

    ProductDTO update(
            Long id,
            ProductDTO dto
    );

    void delete(Long id);

    Long deleteImage(Long imageId);
}