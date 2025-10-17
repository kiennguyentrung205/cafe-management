package vn.edu.fpt.cafemanagement.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.fpt.cafemanagement.entities.Category;
import vn.edu.fpt.cafemanagement.entities.Product;
import vn.edu.fpt.cafemanagement.services.CategoryService;
import vn.edu.fpt.cafemanagement.services.ProductService;

import java.util.List;

@Controller
public class MenuController {
    CategoryService categoryService;
    ProductService productService;

    public MenuController(CategoryService categoryService, ProductService productService) {
        this.categoryService = categoryService;
        this.productService = productService;
    }

    @GetMapping("/menu")
    public String showMenuPage(Model model, @RequestParam(value = "page", defaultValue = "1") int page) {
        int pageSize = 2;
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<Product> productPage = productService.getAllProductsPage(pageable);
        List<Category> categoryList = categoryService.getCategories();
        List<Product> productList = productService.getAllProducts();
        model.addAttribute("productList", productPage.getContent());
        model.addAttribute("categoryList", categoryList);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        return "menu/menu";
    }
}
