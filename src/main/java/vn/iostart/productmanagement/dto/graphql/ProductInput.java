package vn.iostart.productmanagement.dto.graphql;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductInput {

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 200, message = "Tên sản phẩm tối đa 200 ký tự")
    private String name;

    @NotNull(message = "Giá không được để trống")
    @DecimalMin(
        value = "0.0",
        message = "Giá phải lớn hơn hoặc bằng 0"
    )
    private BigDecimal price;

    @NotNull(message = "Số lượng không được để trống")
    @Min(
        value = 0,
        message = "Số lượng phải lớn hơn hoặc bằng 0"
    )
    private Integer quantity;

    @Size(max = 1000, message = "Mô tả tối đa 1000 ký tự")
    private String description;

    @NotNull(message = "Bạn phải chọn danh mục")
    private Long categoryId;
}