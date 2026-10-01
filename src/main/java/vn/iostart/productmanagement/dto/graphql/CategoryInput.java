package vn.iostart.productmanagement.dto.graphql;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryInput {

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(
        max = 255,
        message = "Tên danh mục không được vượt quá 255 ký tự"
    )
    private String categoryName;

    @Size(
        max = 500,
        message = "Đường dẫn icon không được vượt quá 500 ký tự"
    )
    private String icon;
}