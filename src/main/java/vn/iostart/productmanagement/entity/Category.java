package vn.iostart.productmanagement.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(
        max = 255,
        message = "Tên danh mục không được vượt quá 255 ký tự"
    )
    @Column(
        name = "category_name",
        nullable = false,
        unique = true,
        columnDefinition = "NVARCHAR(255)"
    )
    private String categoryName;

    @Size(
        max = 500,
        message = "Đường dẫn ảnh không được vượt quá 500 ký tự"
    )
    @Column(length = 500)
    private String icon;

    @OneToMany(mappedBy = "category")
    @Builder.Default
    private List<Product> products = new ArrayList<>();
}