package vn.iostart.productmanagement.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import vn.iostart.productmanagement.dto.ProductDTO;
import vn.iostart.productmanagement.entity.Category;
import vn.iostart.productmanagement.entity.Product;
import vn.iostart.productmanagement.entity.ProductImage;
import vn.iostart.productmanagement.mapper.ProductMapper;
import vn.iostart.productmanagement.repository.CategoryRepository;
import vn.iostart.productmanagement.repository.ProductImageRepository;
import vn.iostart.productmanagement.repository.ProductRepository;
import vn.iostart.productmanagement.service.ProductService;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of(".jpg", ".jpeg", ".png", ".webp");

    private final ProductRepository productRepository;
    private final ProductImageRepository imageRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    private final Path uploadDir =
            Paths.get("uploads", "products");

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> findAll(
            String keyword,
            int page,
            int size
    ) {
        return search(
                keyword,
                null,
                page,
                size
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> search(
            String keyword,
            Long categoryId,
            int page,
            int size
    ) {
        if (page < 0) {
            page = 0;
        }

        if (size <= 0) {
            size = 5;
        }

        String normalizedKeyword =
                keyword == null
                        ? ""
                        : keyword.trim();

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("id").descending()
        );

        Page<Product> products;

        if (categoryId != null
                && !normalizedKeyword.isBlank()) {

            products = productRepository
                    .findByCategoryCategoryIdAndNameContainingIgnoreCase(
                            categoryId,
                            normalizedKeyword,
                            pageable
                    );

        } else if (categoryId != null) {

            products = productRepository
                    .findByCategoryCategoryId(
                            categoryId,
                            pageable
                    );

        } else if (!normalizedKeyword.isBlank()) {

            products = productRepository
                    .findByNameContainingIgnoreCase(
                            normalizedKeyword,
                            pageable
                    );

        } else {
            products =
                    productRepository.findAll(pageable);
        }

        return products.map(productMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> findAllByPriceAsc() {
        return productRepository
                .findAllByOrderByPriceAsc()
                .stream()
                .map(productMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> findByCategory(
            Long categoryId
    ) {
        if (categoryId == null) {
            throw new IllegalArgumentException(
                    "ID danh mục không được để trống"
            );
        }

        if (!categoryRepository.existsById(categoryId)) {
            throw new RuntimeException(
                    "Không tìm thấy danh mục có ID: "
                            + categoryId
            );
        }

        return productRepository
                .findByCategoryCategoryIdOrderByPriceAsc(
                        categoryId
                )
                .stream()
                .map(productMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO findById(Long id) {
        Product product = findProductEntity(id);

        return productMapper.toDTO(product);
    }

    @Override
    public ProductDTO create(ProductDTO dto) {
        try {
            Category category =
                    findCategoryEntity(
                            dto.getCategoryId()
                    );

            Product product =
                    productMapper.toEntity(
                            dto,
                            category
                    );

            product.setId(null);

            Product saved =
                    productRepository.save(product);

            saveImages(
                    saved,
                    dto.getImageFiles()
            );

            Product result =
                    productRepository.save(saved);

            return productMapper.toDTO(result);

        } catch (IOException exception) {
            throw new RuntimeException(
                    "Không thể tải hình ảnh lên",
                    exception
            );
        }
    }

    @Override
    public ProductDTO update(
            Long id,
            ProductDTO dto
    ) {
        try {
            Product product =
                    findProductEntity(id);

            Category category =
                    findCategoryEntity(
                            dto.getCategoryId()
                    );

            /*
             * Cập nhật thông tin và danh mục sản phẩm.
             */
            productMapper.updateEntity(
                    dto,
                    product,
                    category
            );

            List<MultipartFile> newImages =
                    dto.getImageFiles();

            boolean hasNewImages =
                    newImages != null
                    && newImages.stream()
                            .anyMatch(file ->
                                    file != null
                                    && !file.isEmpty()
                            );

            /*
             * Nếu có chọn ảnh mới thì thay thế
             * toàn bộ ảnh cũ.
             *
             * Nếu không chọn ảnh mới thì giữ nguyên
             * ảnh hiện tại.
             */
            if (hasNewImages) {
                validateImages(newImages);

                deleteExistingImages(product);

                saveImages(
                        product,
                        newImages
                );
            }

            Product updated =
                    productRepository.save(product);

            return productMapper.toDTO(updated);

        } catch (IOException exception) {
            throw new RuntimeException(
                    "Không thể cập nhật hình ảnh",
                    exception
            );
        }
    }

    @Override
    public void delete(Long id) {
        Product product =
                findProductEntity(id);

        if (product.getImages() != null) {
            for (ProductImage image :
                    product.getImages()) {

                deleteFile(image.getImageUrl());
            }
        }

        productRepository.delete(product);
    }

    @Override
    public Long deleteImage(Long imageId) {
        ProductImage image = imageRepository
                .findById(imageId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy hình ảnh"
                        )
                );

        Product product =
                image.getProduct();

        Long productId =
                product.getId();

        boolean wasPrimary =
                Boolean.TRUE.equals(
                        image.getPrimary()
                );

        deleteFile(image.getImageUrl());

        product.getImages()
                .removeIf(current ->
                        Objects.equals(
                                current.getId(),
                                imageId
                        )
                );

        reorderImages(product);

        if (wasPrimary
                && !product.getImages().isEmpty()) {

            product.getImages()
                    .get(0)
                    .setPrimary(true);
        }

        productRepository.save(product);

        return productId;
    }

    private Product findProductEntity(Long id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "ID sản phẩm không được để trống"
            );
        }

        return productRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy sản phẩm có ID: "
                                        + id
                        )
                );
    }

    private Category findCategoryEntity(
            Long categoryId
    ) {
        if (categoryId == null) {
            throw new IllegalArgumentException(
                    "Bạn phải chọn danh mục"
            );
        }

        return categoryRepository
                .findById(categoryId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy danh mục có ID: "
                                        + categoryId
                        )
                );
    }

    private void saveImages(
            Product product,
            List<MultipartFile> files
    ) throws IOException {
        if (files == null || files.isEmpty()) {
            return;
        }

        int displayOrder =
                product.getImages().size();

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            String fileName =
                    saveImage(file);

            ProductImage image =
                    ProductImage.builder()
                            .product(product)
                            .imageUrl(fileName)
                            .primary(
                                    product.getImages()
                                            .isEmpty()
                            )
                            .displayOrder(displayOrder++)
                            .createdAt(
                                    LocalDateTime.now()
                            )
                            .build();

            product.getImages().add(image);
        }
    }

    /**
     * Xóa toàn bộ ảnh cũ của sản phẩm.
     * Bao gồm file vật lý và quan hệ trong entity.
     */
    private void deleteExistingImages(
            Product product
    ) {
        if (product.getImages() == null
                || product.getImages().isEmpty()) {
            return;
        }

        List<ProductImage> oldImages =
                List.copyOf(
                        product.getImages()
                );

        for (ProductImage image : oldImages) {
            deleteFile(image.getImageUrl());
        }

        /*
         * Vì Product.images có orphanRemoval = true,
         * clear() sẽ xóa các bản ghi ảnh cũ.
         */
        product.getImages().clear();
    }

    /**
     * Kiểm tra toàn bộ file trước khi xóa ảnh cũ.
     */
    private void validateImages(
            List<MultipartFile> files
    ) {
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            String extension =
                    getExtension(
                            file.getOriginalFilename()
                    );

            if (!ALLOWED_EXTENSIONS.contains(
                    extension
            )) {
                throw new IllegalArgumentException(
                        "Chỉ chấp nhận ảnh JPG, JPEG, "
                                + "PNG hoặc WEBP"
                );
            }
        }
    }

    private String saveImage(
            MultipartFile file
    ) throws IOException {
        String originalName =
                file.getOriginalFilename();

        String extension =
                getExtension(originalName);

        if (!ALLOWED_EXTENSIONS.contains(
                extension
        )) {
            throw new IllegalArgumentException(
                    "Chỉ chấp nhận ảnh JPG, JPEG, "
                            + "PNG hoặc WEBP"
            );
        }

        Files.createDirectories(uploadDir);

        String fileName =
                UUID.randomUUID() + extension;

        Path uploadRoot =
                uploadDir
                        .toAbsolutePath()
                        .normalize();

        Path target =
                uploadRoot
                        .resolve(fileName)
                        .normalize();

        if (!target.startsWith(uploadRoot)) {
            throw new SecurityException(
                    "Tên file không hợp lệ"
            );
        }

        try (InputStream inputStream =
                     file.getInputStream()) {

            Files.copy(
                    inputStream,
                    target,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }

        return fileName;
    }

    private String getExtension(
            String originalName
    ) {
        if (originalName == null
                || !originalName.contains(".")) {
            return "";
        }

        return originalName
                .substring(
                        originalName.lastIndexOf(".")
                )
                .toLowerCase(Locale.ROOT);
    }

    private void reorderImages(
            Product product
    ) {
        for (int index = 0;
             index < product.getImages().size();
             index++) {

            ProductImage image =
                    product.getImages().get(index);

            image.setDisplayOrder(index);

            /*
             * Chỉ ảnh đầu tiên là ảnh chính.
             */
            image.setPrimary(index == 0);
        }
    }

    private void deleteFile(
            String fileName
    ) {
        if (fileName == null
                || fileName.isBlank()) {
            return;
        }

        try {
            Path uploadRoot =
                    uploadDir
                            .toAbsolutePath()
                            .normalize();

            Path file =
                    uploadRoot
                            .resolve(fileName)
                            .normalize();

            if (!file.startsWith(uploadRoot)) {
                throw new SecurityException(
                        "Tên file không hợp lệ"
                );
            }

            Files.deleteIfExists(file);

        } catch (IOException exception) {
            System.err.println(
                    "Không thể xóa file: "
                            + fileName
            );
        }
    }
}