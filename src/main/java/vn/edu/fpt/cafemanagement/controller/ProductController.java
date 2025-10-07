package vn.edu.fpt.cafemanagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.fpt.cafemanagement.entities.Category;
import vn.edu.fpt.cafemanagement.service.CategoryService;
import vn.edu.fpt.cafemanagement.service.ProductService;

@Controller
@RequestMapping(value = "/product")
public class ProductController {

    private ProductService productService;
    private CategoryService categoryService;

    public ProductController(CategoryService categoryService, ProductService productService) {
        this.categoryService = categoryService;
        this.productService = productService;
    }


    @RequestMapping(value = "/list")
    public String list(Model model) {
        model.addAttribute("title", "Product List");
        model.addAttribute("productList", productService.getActiveProducts());
        model.addAttribute("viewType", "all");
        model.addAttribute("categoryList", categoryService.getCategories());
        return "product/list";
    }

    @RequestMapping(value = "/list/category/{id}")
    public String listByCategory(@PathVariable("id") int categoryId, Model model) {
        model.addAttribute("title", "Products by Category");
        model.addAttribute("productList", productService.getProductsByCategory(categoryId));
        model.addAttribute("viewType", "category");
        model.addAttribute("categoryList", categoryService.getCategories());
        model.addAttribute("categoryId", categoryId); // lưu category hiện tại

        Category category = categoryService.getCategoryById(categoryId);

        model.addAttribute("category", category);
        return "product/list";

    }

    @RequestMapping(value = "/details/{id}")
    public String productDetails(@PathVariable("id") int pro_id, Model model) {
        model.addAttribute("product", productService.getProductsByCategory(pro_id));

        return "product/details";
    }

    @GetMapping(value = "/edit/{proId}")
    public String editProduct(@PathVariable("proId") int proId, Model model) {
        model.addAttribute("title", "Edit Product");
        model.addAttribute("product", productService.getProductById(proId));
        model.addAttribute("categoryList", categoryService.getCategories());

        return "product/edit";
    }


}