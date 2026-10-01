package vn.iostart.productmanagement.controller.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import vn.iostart.productmanagement.dto.ApiResponse;
import vn.iostart.productmanagement.dto.ProductDTO;
import vn.iostart.productmanagement.service.ProductService;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductImageApiController {

    private static final long MAX_FILE_SIZE =
            5L * 1024 * 1024;

    private final ProductService productService;

    @PostMapping(
        value = "/{id}/images",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> uploadImages(
            @PathVariable Long id,
            @RequestParam("images")
            List<MultipartFile> images
    ) {
        if (images == null || images.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error(
                            "Bạn chưa chọn hình ảnh"
                    ));
        }

        for (MultipartFile image : images) {
            if (image == null || image.isEmpty()) {
                continue;
            }

            if (image.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity
                        .badRequest()
                        .body(ApiResponse.error(
                                "Mỗi hình ảnh tối đa 5 MB"
                        ));
            }
        }

        try {
            ProductDTO product =
                    productService.findById(id);

            /*
             * ProductServiceImpl.update() hiện tại đã có
             * saveImages(product, dto.getImageFiles()),
             * nên ta tận dụng lại.
             */
            product.setImageFiles(images);

            ProductDTO updatedProduct =
                    productService.update(id, product);

            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Upload hình ảnh thành công",
                            updatedProduct
                    )
            );

        } catch (IllegalArgumentException exception) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error(
                            exception.getMessage()
                    ));

        } catch (RuntimeException exception) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(
                            exception.getMessage()
                    ));
        }
    }
}