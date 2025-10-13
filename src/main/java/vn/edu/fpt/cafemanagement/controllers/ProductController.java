package vn.edu.fpt.cafemanagement.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.fpt.cafemanagement.entities.Category;
import vn.edu.fpt.cafemanagement.entities.Product;
import vn.edu.fpt.cafemanagement.services.CategoryService;
import vn.edu.fpt.cafemanagement.services.ProductService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping(value = "/product")
public class ProductController {

    private ProductService productService;
    private CategoryService categoryService;

    public ProductController(CategoryService categoryService, ProductService productService) {
        this.categoryService = categoryService;
        this.productService = productService;
    }


//


    @GetMapping(value = "/list")
    public String showList(
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            Model model
    ) {
        model.addAttribute("title", "Product List");

        // 1. Logic Lọc Sản phẩm
        List<Product> products;
        if (categoryId != null && categoryId > 0) {
            // Nếu có ID, gọi phương thức Service để lọc theo Category ID
            products = productService.getProductsByCategory(categoryId);
        } else {
            // Mặc định: Lấy tất cả sản phẩm đang hoạt động
            products = productService.getActiveProducts();
        }

        model.addAttribute("productList", products);

        // 2. Thêm Category List để hiển thị ô chọn
        model.addAttribute("categoryList", categoryService.getCategories());

        // 3. Thêm ID đã chọn vào Model để Thymeleaf có thể giữ trạng thái
        model.addAttribute("selectedCategoryId", categoryId);

        return "product/list";
    }

    @GetMapping(value = "/edit/{proId}")
    public String showEditForm(@PathVariable("proId") int proId, Model model) {
        model.addAttribute("title", "Edit Product");
        model.addAttribute("product", productService.getProductById(proId));
        model.addAttribute("categoryList", categoryService.getCategories());

        return "product/edit";
    }

    private final String UPLOAD_DIR = "D:/SWP/Project/uploads/";

    // Ví dụ về ProductService
    // @Autowired
    // private ProductService productService;

    @PostMapping("/edit")
    public String updateProduct(@ModelAttribute("product") Product product,
                                @RequestParam("file") MultipartFile file) { // Tên "file" phải khớp với name="file" trong HTML

        // BƯỚC 1: Xử lý file ảnh mới (nếu người dùng có chọn)
        if (!file.isEmpty()) {
            try {
                // 1. Tạo tên file mới và duy nhất (để tránh trùng lặp)
                String originalFileName = file.getOriginalFilename();
                // Lấy phần mở rộng (extension) của file, ví dụ: .jpg, .png
                String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
                // Tạo tên file mới bằng UUID
                String newFileName = UUID.randomUUID().toString() + extension;

                // 2. Định nghĩa đường dẫn file trên ổ đĩa
                Path targetPath = Paths.get(UPLOAD_DIR, newFileName);

                // 3. **Lưu file** vào thư mục bên ngoài project
                file.transferTo(targetPath);


                product.setImg(newFileName);

            } catch (IOException e) {
                // Xử lý lỗi khi lưu file (ví dụ: log lỗi, trả về trang edit với thông báo)
                e.printStackTrace();
            }
        }
        // ELSE: Nếu file.isEmpty() (người dùng không chọn file mới),
        // thì trường product.getImg() vẫn giữ lại tên file CŨ
        // nhờ vào input hidden trong form. KHÔNG CẦN làm gì thêm.

        // BƯỚC 2: Lưu Product (đã có tên ảnh mới hoặc cũ) vào Database
        productService.saveProduct(product);

        return "redirect:/product/list";
    }

    @GetMapping(value = "/create")
    public String showCreateForm(Model model) {

        model.addAttribute("title", "Create Product");
        model.addAttribute("product", new Product());
        model.addAttribute("categoryList", categoryService.getCategories());

        return "product/create";
    }

    @PostMapping(value = "/create")
    public String createProduct(@ModelAttribute("product") Product product,
                                @RequestParam("file") MultipartFile file) { // Tên "file" phải khớp với name="file" trong HTML

        // BƯỚC 1: Xử lý file ảnh mới (nếu người dùng có chọn)
        if (!file.isEmpty()) {
            try {
                // 1. Tạo tên file mới và duy nhất (để tránh trùng lặp)
                String originalFileName = file.getOriginalFilename();
                // Lấy phần mở rộng (extension) của file, ví dụ: .jpg, .png
                String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
                // Tạo tên file mới bằng UUID
                String newFileName = UUID.randomUUID().toString() + extension;

                // 2. Định nghĩa đường dẫn file trên ổ đĩa
                Path targetPath = Paths.get(UPLOAD_DIR, newFileName);

                // 3. **Lưu file** vào thư mục bên ngoài project
                file.transferTo(targetPath);


                product.setImg(newFileName);

            } catch (IOException e) {
                // Xử lý lỗi khi lưu file (ví dụ: log lỗi, trả về trang edit với thông báo)
                e.printStackTrace();
            }
        }
        // ELSE: Nếu file.isEmpty() (người dùng không chọn file mới),
        // thì trường product.getImg() vẫn giữ lại tên file CŨ
        // nhờ vào input hidden trong form. KHÔNG CẦN làm gì thêm.

        // BƯỚC 2: Lưu Product (đã có tên ảnh mới hoặc cũ) vào Database
        product.setStatus("Available");
        product.setActive(true);
        productService.saveProduct(product);
        return "redirect:/product/list";
    }

    @PostMapping(value = "/delete/{id}")
    public String deleteProduct(@PathVariable("id") int proId) {

        productService.deleteSortProduct(productService.getProductById(proId));
        return "redirect:/product/list";
    }

    @GetMapping(value = "/deleted-list")
    public String viewDeletedProductList(
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            Model model
    ) {
        model.addAttribute("title", "Product List");

        // 1. Logic Lọc Sản phẩm
        List<Product> products;
        if (categoryId != null && categoryId > 0) {
            // Nếu có ID, gọi phương thức Service để lọc theo Category ID
            products = productService.getNonActiveProductsByCategory(categoryId);
        } else {

            products = productService.getNonActiveqProducts();
        }

        model.addAttribute("productList", products);

        // 2. Thêm Category List để hiển thị ô chọn
        model.addAttribute("categoryList", categoryService.getCategories());

        // 3. Thêm ID đã chọn vào Model để Thymeleaf có thể giữ trạng thái
        model.addAttribute("selectedCategoryId", categoryId);

        return "product/deleted-list";
    }
}