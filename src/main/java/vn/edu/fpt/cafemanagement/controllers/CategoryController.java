package vn.edu.fpt.cafemanagement.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.cafemanagement.entities.Category;
import vn.edu.fpt.cafemanagement.repositories.CategoryRepository;
import vn.edu.fpt.cafemanagement.services.CategoryService;

@Controller
@RequestMapping(value = "/category")
public class CategoryController {

    private CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping(value = "/list")
    public String showCategoryList(Model model) {

        model.addAttribute("CategoryList", categoryService.getCategories());
        return "category/list";
    }

    @GetMapping(value = "/create")
    public String showCreateForm(Model model) {
        model.addAttribute("title", "Create Category");
        model.addAttribute("category", new Category());
        return "/category/create";
    }

    @GetMapping(value = "/edit/{id}")
    public String showEditForm(@PathVariable("id") int id, Model model) {
        model.addAttribute("title", "Edit Category");
        model.addAttribute("category", categoryService.getCategoryById(id));

        return "category/edit";
    }

    @GetMapping(value = "/deleted-list")
    public String viewDeletedCategories(Model model) {
        model.addAttribute("title", "Category List");
        model.addAttribute("categoryList", categoryService.getNonActiveCategories());
        return "category/deleted-list";
    }

    @PostMapping(value = "/edit")
    public String editCategory(@ModelAttribute("category") Category category) {
        categoryService.saveCategory(category);
        return "redirect:/category/list";
    }

    @PostMapping(value = "/delete/{id}")
    public String deleteCategory(@PathVariable("id") int id, Model model) {
        categoryService.deleteSortCategory(categoryService.getCategoryById(id));
        return "redirect:/category/list";
    }

    @PostMapping(value = "/create")
    public String createCategory(@ModelAttribute("category") Category category) {
        category.setActive(true);
        categoryService.saveCategory(category);
        return "redirect:/category/list";
    }
}
