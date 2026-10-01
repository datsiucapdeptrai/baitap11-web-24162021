package vn.iostart.productmanagement.controller;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.iostart.productmanagement.dto.ProductDTO;
import vn.iostart.productmanagement.service.ProductService;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public String listProducts(
            @RequestParam(
                name = "keyword",
                defaultValue = ""
            )
            String keyword,

            @RequestParam(
                name = "page",
                defaultValue = "0"
            )
            int page,

            @RequestParam(
                name = "size",
                defaultValue = "5"
            )
            int size,

            Model model
    ) {
        Page<ProductDTO> productPage =
                productService.findAll(keyword, page, size);

        model.addAttribute("products", productPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("size", size);

        return "products/list";
    }

    @GetMapping("/{id}")
    public String detail(
            @PathVariable("id") Long id,
            Model model
    ) {
        model.addAttribute(
                "product",
                productService.findById(id)
        );

        return "products/detail";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("product", new ProductDTO());
        model.addAttribute("formTitle", "Thêm sản phẩm");

        return "products/form";
    }

    @PostMapping(
        value = "/create",
        consumes = "multipart/form-data"
    )
    public String create(
            @Valid
            @ModelAttribute("product")
            ProductDTO productDTO,

            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Thêm sản phẩm");
            return "products/form";
        }

        try {
            productService.create(productDTO);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Thêm sản phẩm thành công"
            );

            return "redirect:/products";

        } catch (RuntimeException exception) {
            model.addAttribute("formTitle", "Thêm sản phẩm");
            model.addAttribute("error", exception.getMessage());

            return "products/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String editForm(
            @PathVariable("id") Long id,
            Model model
    ) {
        model.addAttribute(
                "product",
                productService.findById(id)
        );

        model.addAttribute(
                "formTitle",
                "Cập nhật sản phẩm"
        );

        return "products/form";
    }

    @PostMapping(
        value = "/edit/{id}",
        consumes = "multipart/form-data"
    )
    public String update(
            @PathVariable("id") Long id,

            @Valid
            @ModelAttribute("product")
            ProductDTO productDTO,

            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            ProductDTO existing =
                    productService.findById(id);

            productDTO.setImages(existing.getImages());

            model.addAttribute("formTitle", "Cập nhật sản phẩm");
            return "products/form";
        }

        try {
            productService.update(id, productDTO);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Cập nhật sản phẩm thành công"
            );

            return "redirect:/products/" + id;

        } catch (RuntimeException exception) {
            ProductDTO existing =
                    productService.findById(id);

            productDTO.setImages(existing.getImages());

            model.addAttribute("formTitle", "Cập nhật sản phẩm");
            model.addAttribute("error", exception.getMessage());

            return "products/form";
        }
    }

    @PostMapping("/image/delete/{imageId}")
    public String deleteImage(
            @PathVariable("imageId") Long imageId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Long productId =
                    productService.deleteImage(imageId);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Xóa hình ảnh thành công"
            );

            return "redirect:/products/edit/" + productId;

        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    exception.getMessage()
            );

            return "redirect:/products";
        }
    }

    @PostMapping("/delete/{id}")
    public String delete(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            productService.delete(id);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Xóa sản phẩm thành công"
            );

        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    exception.getMessage()
            );
        }

        return "redirect:/products";
    }
}