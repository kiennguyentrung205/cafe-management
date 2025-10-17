package vn.edu.fpt.cafemanagement.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
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
import java.util.Arrays;
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

    private final String UPLOAD_DIR = "D:/SWP/Project/uploads/";

    private static final String VIETNAMESE_NAME_PATTERN = "^[\\p{L}\\s]+$";

    @GetMapping(value = "/list")
    public String showList(
            // NHẬN DƯỚI DẠNG STRING để bắt lỗi người dùng nhập chữ
            @RequestParam(value = "categoryId", required = false) String categoryIdStr, @RequestParam(name = "page", defaultValue = "1") int page,
            Model model) {

        model.addAttribute("title", "Product List");
        int size = 10;
        if (page < 1) {
            page = 1;
        }

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Product> list = productService.getAllProductsPaged(pageable);
        if (page > list.getTotalPages()) {
            page = list.getTotalPages();
            pageable = PageRequest.of(page - 1, size);
            list = productService.getAllProductsPaged(pageable);

        }


        // BƯỚC 1: XỬ LÝ VÀ CHUYỂN ĐỔI THAM SỐ (Bắt lỗi nhập chữ)
        Integer tempCategoryId = 0;
        if (categoryIdStr != null && !categoryIdStr.trim().isEmpty()) {
            try {
                tempCategoryId = Integer.parseInt(categoryIdStr);
            } catch (NumberFormatException e) {
                System.err.println("Cảnh báo bảo mật: categoryId không phải là số. Mặc định về 0.");
                tempCategoryId = 0;
            }
        }

        // TẠO BIẾN FINAL: Đây là giá trị cuối cùng được sử dụng trong lambda (để tránh lỗi)
        final Integer finalCategoryId = tempCategoryId;

        // BƯỚC 2: KIỂM TRA TÍNH HỢP LỆ CỦA ID (Bắt lỗi thao túng ID không tồn tại)
        List<Category> categoryList = categoryService.getCategories();

        // Kiểm tra: ID phải là ID hợp lệ, hoặc là ID = 0 (All Categories)
        boolean isValidCategory = categoryList.stream()
                .anyMatch(c -> c.getCateId() == finalCategoryId) || finalCategoryId == 0;

        // BƯỚC 3: LỌC SẢN PHẨM
        Integer categoryIdForModel = finalCategoryId; // Biến này dùng để truyền về View

        if (finalCategoryId > 0 && isValidCategory) {
            list = productService.getProductsByCategory(finalCategoryId, pageable);
        } else {
            // Mặc định cho ID = 0, ID < 0, ID không hợp lệ
            list = productService.getAllProductsPaged(pageable);
            categoryIdForModel = 0;
        }

        // BƯỚC 4: TRUYỀN DỮ LIỆU VỀ VIEW

        model.addAttribute("pageProduct", list);
        model.addAttribute("categoryList", categoryList);
        model.addAttribute("selectedCategoryId", categoryIdForModel);

        return "product/list";
    }


    @GetMapping(value = "/deleted-list")
    public String showDeletedList(
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            Model model
    ) {
        model.addAttribute("title", "Deleted Product List");

        // 1. Logic Lọc Sản phẩm
        List<Product> products;
        if (categoryId != null && categoryId > 0) {
            // Nếu có ID, gọi phương thức Service để lọc theo Category ID
            products = productService.getProductsByCategory(categoryId);
        } else {
            // Mặc định: Lấy tất cả sản phẩm đang hoạt động
            products = productService.getNonActiveProducts();
        }

        model.addAttribute("productList", products);

        // 2. Thêm Category List để hiển thị ô chọn
        model.addAttribute("categoryList", categoryService.getCategories());

        // 3. Thêm ID đã chọn vào Model để Thymeleaf có thể giữ trạng thái
        model.addAttribute("selectedCategoryId", categoryId);

        return "product/deleted-list";
    }


    @GetMapping(value = "/search")
    public String searchProduct(Model model, @RequestParam("keyword") String keyword, @RequestParam(name = "page", defaultValue = "1") int page) {
        int size = 10;
        if (page < 1) {
            page = 1;
        }

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Product> list = productService.getSearchProducts(keyword, pageable);

        // Xử lý khi kết quả rỗng (totalPages = 0) hoặc page vượt quá
        if (list.getTotalPages() > 0 && page > list.getTotalPages()) {
            page = list.getTotalPages();
            pageable = PageRequest.of(page - 1, size);
            list = productService.getSearchProducts(keyword, pageable);
        }

        // Đảm bảo page luôn hợp lệ (đặc biệt khi totalPages = 0)
        if (page < 1) {
            page = 1;
        }

        model.addAttribute("title", "Search Product");
        model.addAttribute("categoryList", categoryService.getCategories());
        model.addAttribute("pageProduct", list);

        // QUAN TRỌNG: Truyền keyword và thiết lập selectedCategoryId cho View
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryId", 0);

        return "product/list";
    }

    @GetMapping(value = "/edit/{proId}")
    public String showEditForm(@PathVariable("proId") String idStr, Model model) {

        Integer proId = null;

        try {
            proId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            // ID là chữ! Gán null hoặc -1 để đảm bảo productService trả về null
            System.err.println("ID nhập vào không phải là số: " + idStr);
            proId = -1; // Hoặc một giá trị chắc chắn không tồn tại
        }


        model.addAttribute("title", "Edit Product");
        model.addAttribute("product", productService.getProductById(proId));
        model.addAttribute("categoryList", categoryService.getCategories());

        return "product/edit";
    }


    // Ví dụ về ProductService
    // @Autowired
    // private ProductService productService;

    @PostMapping("/edit")
    public String updateProduct(@ModelAttribute("product") Product product, BindingResult bindingResult,
                                @RequestParam("file") MultipartFile file, Model model) { // Tên "file" phải khớp với name="file" trong HTML

        boolean hasError = false;

        // Validation Name
        String proName = product.getProName();
        if (proName == null || proName.trim().isEmpty()) {
            model.addAttribute("nameError", "Product name cant be empty");
            hasError = true;
        } else if (!proName.matches(VIETNAMESE_NAME_PATTERN)) {
            model.addAttribute("nameError", "Name just can contain letters!");
            hasError = true;
        }

        //Validation price
        if (bindingResult.hasFieldErrors("price")) {
            // Lỗi xảy ra khi Spring KHÔNG THỂ chuyển chuỗi từ form thành double (ví dụ: nhập chữ, bỏ trống)
            // Lấy thông báo lỗi cụ thể (thường là TypeMismatch)
            FieldError priceError = bindingResult.getFieldError("price");

            // Trường hợp lỗi binding là do nhập chữ hoặc bỏ trống.
            model.addAttribute("priceError", "Giá sản phẩm không hợp lệ. Vui lòng nhập một số.");
            hasError = true;
        }
        if (!hasError) {
            double priceValue = product.getPrice(); // Lấy giá trị double

            // Kiểm tra giá trị tối thiểu
            if (priceValue < 1000) {
                // Lỗi này bao gồm cả trường hợp người dùng nhập 0 (vì 0 < 1000)
                model.addAttribute("priceError", "Giá sản phẩm phải tối thiểu 1000.");
                hasError = true;
            }
        }


        //Validation Description

        String description = product.getDescription();
        if (!description.matches(VIETNAMESE_NAME_PATTERN) && description.trim().length() > 0) {
            model.addAttribute("descriptionError", "Description just can contain letters!");
            hasError = true;
        }


        // BƯỚC 1: Xử lý file ảnh mới (nếu người dùng có chọn)
        if (!file.isEmpty()) {

            // --- BẮT ĐẦU VALIDATION FILE ---

            // 1. Kiểm tra Kích thước file (ví dụ: Max 5MB)
            final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
            if (file.getSize() > MAX_FILE_SIZE) {
                model.addAttribute("fileError", "Kích thước file vượt quá giới hạn cho phép (5MB).");
                hasError = true;
            }

            // 2. Kiểm tra Loại file (MIME Type)
            List<String> allowedContentTypes = Arrays.asList("image/jpeg", "image/png", "image/gif");
            if (!allowedContentTypes.contains(file.getContentType())) {
                model.addAttribute("fileError", "File không phải là định dạng hình ảnh hợp lệ (JPEG, PNG, GIF).");
                hasError = true;
            }

            // --- KẾT THÚC VALIDATION FILE ---

            // Chỉ tiếp tục lưu file nếu KHÔNG có lỗi validation (hasError vẫn là false)
            if (!hasError) {
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
                    // Xử lý lỗi khi lưu file (lỗi I/O, lỗi hệ thống)
                    model.addAttribute("fileError", "Lỗi hệ thống khi lưu file ảnh lên ổ đĩa.");
                    hasError = true; // Đánh dấu lỗi hệ thống
                    e.printStackTrace();
                }
            }
        }
        // ELSE: Nếu file.isEmpty() (người dùng không chọn file mới),
        // thì trường product.getImg() vẫn giữ lại tên file CŨ
        // nhờ vào input hidden trong form. KHÔNG CẦN làm gì thêm.

        if (hasError) {
            // Đảm bảo truyền lại danh sách category nếu có lỗi
            // model.addAttribute("categoryList", categoryService.findAll());
            model.addAttribute("categoryList", categoryService.getCategories());
            return "/product/edit";
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
    public String createProduct(@ModelAttribute("product") Product product, BindingResult bindingResult,
                                @RequestParam("file") MultipartFile file, Model model) { // Tên "file" phải khớp với name="file" trong HTML
        boolean hasError = false;

        // Validation Name
        String proName = product.getProName();
        if (proName == null || proName.trim().isEmpty()) {
            model.addAttribute("nameError", "Product name cant be empty");
            hasError = true;
        } else if (!proName.matches(VIETNAMESE_NAME_PATTERN)) {
            model.addAttribute("nameError", "Name just can contain letters!");
            hasError = true;
        }

        //Validation price
        if (bindingResult.hasFieldErrors("price")) {
            // Lỗi xảy ra khi Spring KHÔNG THỂ chuyển chuỗi từ form thành double (ví dụ: nhập chữ, bỏ trống)
            // Lấy thông báo lỗi cụ thể (thường là TypeMismatch)
            FieldError priceError = bindingResult.getFieldError("price");

            // Trường hợp lỗi binding là do nhập chữ hoặc bỏ trống.
            model.addAttribute("priceError", "Giá sản phẩm không hợp lệ. Vui lòng nhập một số.");
            hasError = true;
        }
        if (!hasError) {
            double priceValue = product.getPrice(); // Lấy giá trị double

            // Kiểm tra giá trị tối thiểu
            if (priceValue < 1000) {
                // Lỗi này bao gồm cả trường hợp người dùng nhập 0 (vì 0 < 1000)
                model.addAttribute("priceError", "Giá sản phẩm phải tối thiểu 1000.");
                hasError = true;
            }
        }


        //Validation Description

        String description = product.getDescription();
        if (!description.matches(VIETNAMESE_NAME_PATTERN) && description.trim().length() > 0) {
            model.addAttribute("descriptionError", "Description just can contain letters!");
            hasError = true;
        }


        // BƯỚC 1: Xử lý file ảnh mới (nếu người dùng có chọn)
        if (!file.isEmpty()) {

            // --- BẮT ĐẦU VALIDATION FILE ---

            // 1. Kiểm tra Kích thước file (ví dụ: Max 5MB)
            final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
            if (file.getSize() > MAX_FILE_SIZE) {
                model.addAttribute("fileError", "Kích thước file vượt quá giới hạn cho phép (5MB).");
                hasError = true;
            }

            // 2. Kiểm tra Loại file (MIME Type)
            List<String> allowedContentTypes = Arrays.asList("image/jpeg", "image/png", "image/gif");
            if (!allowedContentTypes.contains(file.getContentType())) {
                model.addAttribute("fileError", "File không phải là định dạng hình ảnh hợp lệ (JPEG, PNG, GIF).");
                hasError = true;
            }

            // --- KẾT THÚC VALIDATION FILE ---

            // Chỉ tiếp tục lưu file nếu KHÔNG có lỗi validation (hasError vẫn là false)
            if (!hasError) {
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
                    // Xử lý lỗi khi lưu file (lỗi I/O, lỗi hệ thống)
                    model.addAttribute("fileError", "Lỗi hệ thống khi lưu file ảnh lên ổ đĩa.");
                    hasError = true; // Đánh dấu lỗi hệ thống
                    e.printStackTrace();
                }
            }
        }
        // ELSE: Nếu file.isEmpty() (người dùng không chọn file mới),
        // thì trường product.getImg() vẫn giữ lại tên file CŨ
        // nhờ vào input hidden trong form. KHÔNG CẦN làm gì thêm.

        if (hasError) {
            // Đảm bảo truyền lại danh sách category nếu có lỗi
            // model.addAttribute("categoryList", categoryService.findAll());
            model.addAttribute("categoryList", categoryService.getCategories());
            return "/product/create";
        }


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

    @GetMapping(value = "/{id}")
    public String showDetails(@PathVariable("id") String idStr,
                              Model model) {

        model.addAttribute("title", "Product Details");
        Integer proId = null;

        try {
            proId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            // ID là chữ! Gán null hoặc -1 để đảm bảo productService trả về null
            System.err.println("ID nhập vào không phải là số: " + idStr);
            proId = -1; // Hoặc một giá trị chắc chắn không tồn tại
        }

        // Nếu proId là -1, productService.getProductById(-1) sẽ trả về null
        model.addAttribute("product", productService.getProductById(proId));

        // View sẽ xử lý product == null để hiển thị thông báo lỗi (như đã sửa ở câu trước)
        return "product/details";
    }
}