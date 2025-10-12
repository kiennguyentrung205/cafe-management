package vn.edu.fpt.cafemanagement.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.cafemanagement.entities.*;
import vn.edu.fpt.cafemanagement.services.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/order")
public class OrderController {

    private final ProductService productService;
    private final OrderService orderService;
    private final VoucherService voucherService;
    private final CustomerService customerService;
    private final ManagerService managerService;
    private final CategoryService categoryService;
    private final PointHistoryService pointHistoryService;

    public OrderController(ProductService productService,
                           OrderService orderService,
                           VoucherService voucherService,
                           CustomerService customerService,
                           ManagerService managerService,
                           CategoryService categoryService,
                           PointHistoryService pointHistoryService) {
        this.productService = productService;
        this.orderService = orderService;
        this.voucherService = voucherService;
        this.customerService = customerService;
        this.managerService = managerService;
        this.categoryService = categoryService;
        this.pointHistoryService = pointHistoryService;
    }

    // ----------------------- [GET: Hiển thị form tạo order] -----------------------
    @GetMapping("/create")
    public String showCreateOrderForm(
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            Model model) {

        int pageSize = 9;
        Page<Product> productPage;

        // --- Lọc sản phẩm theo Category ---
        if (categoryId != null && categoryId > 0) {
            productPage = productService.getProductsByCategoryPaged(categoryId, PageRequest.of(page - 1, pageSize));
        } else {
            productPage = productService.getActiveProductsPaged(PageRequest.of(page - 1, pageSize));
        }

        // --- Gửi dữ liệu sang View ---
        model.addAttribute("categoryList", categoryService.getCategories());
        model.addAttribute("selectedCategoryId", categoryId != null ? categoryId : 0);
        model.addAttribute("productList", productPage.getContent());
        model.addAttribute("voucherList", voucherService.getActiveVouchers());
        model.addAttribute("customer", null);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("title", "Create In-Store Order");

        return "order/create";
    }

    // ----------------------- [POST: Tạo order mới] -----------------------
    @Transactional
    @PostMapping("/create")
    public String createOrder(
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "productIds", required = false) List<Integer> productIds,
            @RequestParam(value = "quantities", required = false) List<Integer> quantities,
            @RequestParam(value = "voucherId", required = false) Optional<Integer> voucherId,
            @RequestParam(value = "customerPhone", required = false) String customerPhone,
            @RequestParam(value = "pointsUsed", defaultValue = "0") int pointsUsed,
            Model model) {

        // --- Kiểm tra nếu chỉ nhấn "Check" ---
        if ("check".equals(action)) {
            Customer customer = customerService.getCustomerByPhone(customerPhone);
            if (customer == null)
                model.addAttribute("error", "Customer not found!");
            else
                model.addAttribute("customer", customer);
            return reloadCreatePage(model);
        }

        // --- Kiểm tra danh sách sản phẩm ---
        if (productIds == null || productIds.isEmpty()) {
            model.addAttribute("error", "No products selected!");
            return reloadCreatePage(model);
        }

        // --- Kiểm tra khách hàng ---
        Customer customer = customerService.getCustomerByPhone(customerPhone);
        if (customer == null) {
            model.addAttribute("error", "Customer not found!");
            return reloadCreatePage(model);
        }

        if (pointsUsed > customer.getPoint()) {
            model.addAttribute("error", "Not enough points!");
            return reloadCreatePage(model);
        }

        // --- Kiểm tra manager ---
        Manager manager = managerService.getDefaultManager();
        if (manager == null) {
            model.addAttribute("error", "No manager available!");
            return reloadCreatePage(model);
        }

        // --- Tạo Order ---
        Order order = new Order();
        order.setCustomer(customer);
        order.setManager(manager);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus("Pending");
        order.setPointsUsed(pointsUsed);

        // --- Áp dụng voucher ---
        Voucher voucher = null;
        Integer voucherIdValue = voucherId.orElse(0);
        if (voucherIdValue != 0) {
            voucher = voucherService.getVoucherById(voucherIdValue);
            if (voucher == null || voucher.getQuantity() <= 0) {
                model.addAttribute("error", "Voucher invalid or out of stock!");
                return reloadCreatePage(model);
            }
            order.setVoucher(voucher);
            voucher.setQuantity(voucher.getQuantity() - 1);
            voucherService.saveVoucher(voucher);
        }

        // --- Tính tổng giá ---
        double totalPrice = 0;
        List<OrderItem> orderItems = new ArrayList<>();
        for (int i = 0; i < productIds.size(); i++) {
            Product product = productService.getProductById(productIds.get(i));
            int qty = quantities.get(i);
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(qty);
            orderItems.add(item);
            totalPrice += product.getPrice() * qty;
        }

        // --- Áp dụng giảm giá từ voucher ---
        if (voucher != null) {
            if ("PERCENT".equalsIgnoreCase(voucher.getDiscountType()))
                totalPrice -= totalPrice * (voucher.getDiscountValue() / 100.0);
            else if ("AMOUNT".equalsIgnoreCase(voucher.getDiscountType()))
                totalPrice -= voucher.getDiscountValue();
        }

        // --- Giảm theo điểm thưởng ---
        totalPrice -= pointsUsed * 2000;
        if (totalPrice < 0) totalPrice = 0;
        order.setTotalPrice(totalPrice);
        order.setOrderItems(orderItems);

        // --- Cập nhật điểm khách hàng ---
        int earnedPoints = (int) (totalPrice / 50000);
        customer.setPoint(customer.getPoint() - pointsUsed + earnedPoints);
        customerService.saveCustomer(customer);

        // --- Lưu lịch sử điểm ---
        if (pointsUsed > 0) {
            PointHistory ph = new PointHistory();
            ph.setCustomer(customer);
            ph.setOrder(order);
            ph.setAmount(-pointsUsed);
            ph.setTypeOfChange("Redeemed in order");
            ph.setChangeTime(LocalDateTime.now());
            pointHistoryService.saveHistory(ph);
        }

        // --- Lưu Order ---
        orderService.saveOrder(order);

        model.addAttribute("success", "Order created successfully!");
        model.addAttribute("order", order);
        return "order/success";
    }

    // ----------------------- [GET: Danh sách đơn hàng] -----------------------
    @GetMapping("/list")
    public String viewOrders(@RequestParam(defaultValue = "1") int page, Model model) {
        int pageSize = 9;
        Page<Order> orderPage = orderService.getPagedOrders(page, pageSize);

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        return "order/list";
    }

    // ----------------------- [Helper: Reload trang create khi lỗi] -----------------------
    private String reloadCreatePage(Model model) {
        model.addAttribute("categoryList", categoryService.getCategories());
        model.addAttribute("productList", productService.getActiveProducts());
        model.addAttribute("voucherList", voucherService.getActiveVouchers());
        model.addAttribute("customer", null);
        model.addAttribute("currentPage", 1);
        model.addAttribute("totalPages", 1);
        return "order/create";
    }

    @GetMapping("/products")
    public String getProducts(
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            Model model) {

        int pageSize = 9;
        Page<Product> productPage;

        if (categoryId != null && categoryId > 0)
            productPage = productService.getProductsByCategoryPaged(categoryId, PageRequest.of(page - 1, pageSize));
        else
            productPage = productService.getActiveProductsPaged(PageRequest.of(page - 1, pageSize));

        model.addAttribute("productList", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        return "order/_productGrid :: productGrid";
    }


}
