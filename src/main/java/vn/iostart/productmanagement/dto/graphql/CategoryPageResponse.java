package vn.iostart.productmanagement.dto.graphql;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.iostart.productmanagement.dto.CategoryDTO;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryPageResponse {

    private List<CategoryDTO> content;

    private int page;

    private int size;

    private long totalElements;

    private int totalPages;

    private boolean first;

    private boolean last;

    public static CategoryPageResponse from(
            Page<CategoryDTO> categoryPage
    ) {
        return CategoryPageResponse.builder()
                .content(categoryPage.getContent())
                .page(categoryPage.getNumber())
                .size(categoryPage.getSize())
                .totalElements(categoryPage.getTotalElements())
                .totalPages(categoryPage.getTotalPages())
                .first(categoryPage.isFirst())
                .last(categoryPage.isLast())
                .build();
    }
}