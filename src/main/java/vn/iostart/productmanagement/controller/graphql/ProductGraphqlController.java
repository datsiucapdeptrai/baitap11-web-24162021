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
import vn.iostart.productmanagement.dto.ProductDTO;
import vn.iostart.productmanagement.dto.graphql.ProductInput;
import vn.iostart.productmanagement.dto.graphql.ProductPageResponse;
import vn.iostart.productmanagement.service.ProductService;

@Controller
@RequiredArgsConstructor
public class ProductGraphqlController {

    private final ProductService productService;

    @QueryMapping
    public List<ProductDTO> productsByPriceAsc() {
        return productService.findAllByPriceAsc();
    }

    @QueryMapping
    public List<ProductDTO> productsByCategory(
            @Argument("categoryId") Long categoryId
    ) {
        return productService.findByCategory(categoryId);
    }

    @QueryMapping
    public ProductDTO productById(
            @Argument("id") Long id
    ) {
        return productService.findById(id);
    }

    @QueryMapping
    public ProductPageResponse productPage(
            @Argument("keyword") String keyword,
            @Argument("categoryId") Long categoryId,
            @Argument("page") int page,
            @Argument("size") int size
    ) {
        Page<ProductDTO> productPage =
                productService.search(
                        keyword,
                        categoryId,
                        page,
                        size
                );

        return ProductPageResponse.from(productPage);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ProductDTO createProduct(
            @Valid
            @Argument("input") ProductInput input
    ) {
        ProductDTO request = toProductDTO(input);

        return productService.create(request);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ProductDTO updateProduct(
            @Argument("id") Long id,
            @Valid
            @Argument("input") ProductInput input
    ) {
        ProductDTO request = toProductDTO(input);

        return productService.update(id, request);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean deleteProduct(
            @Argument("id") Long id
    ) {
        productService.delete(id);

        return true;
    }

    private ProductDTO toProductDTO(
            ProductInput input
    ) {
        return ProductDTO.builder()
                .name(input.getName().trim())
                .price(input.getPrice())
                .quantity(input.getQuantity())
                .description(
                        input.getDescription() == null
                                ? null
                                : input.getDescription().trim()
                )
                .categoryId(input.getCategoryId())
                .build();
    }
}