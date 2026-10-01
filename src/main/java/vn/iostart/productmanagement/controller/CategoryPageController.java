package vn.iostart.productmanagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CategoryPageController {

    @GetMapping("/categories")
    public String showCategoryPage() {
        return "categories/list";
    }
}