package vn.edu.fpt.cafemanagement.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    // ----------------------- [GET: Hiển thị form tạo order + tìm kiếm + phân trang] -----------------------
    @GetMapping("/create")
    public String showCreateOrderForm(
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "page", defaultValue = "1") int page,
            Model model) {

        int pageSize = 10;
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<Product> productPage;

        if (query != null && !query.trim().isEmpty() && categoryId != null && categoryId > 0) {
            // tìm theo cả tên + category
            productPage = productService.searchActiveProductsByCategory(categoryId, query.trim(), pageable);
        }
        else if (query != null && !query.trim().isEmpty()) {
            // chỉ tìm theo tên
            productPage = productService.searchActiveProducts(query.trim(), pageable);
        }
        else if (categoryId != null && categoryId > 0) {
            // chỉ lọc theo category
            productPage = productService.getProductsByCategoryPaged(categoryId, pageable);
        }
        else {
            productPage = productService.getActiveProductsPaged(pageable);
        }

        model.addAttribute("categoryList", categoryService.getCategories());
        model.addAttribute("selectedCategoryId", categoryId != null ? categoryId : 0);
        model.addAttribute("query", query != null ? query : "");
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

        if ("check".equals(action)) {
            Customer customer = customerService.getCustomerByPhone(customerPhone);
            if (customer == null)
                model.addAttribute("error", "Customer not found!");
            else
                model.addAttribute("customer", customer);
            return reloadCreatePage(model);
        }

        if (productIds == null || productIds.isEmpty()) {
            model.addAttribute("error", "No products selected!");
            return reloadCreatePage(model);
        }

        Customer customer = customerService.getCustomerByPhone(customerPhone);
        if (customer == null) {
            model.addAttribute("error", "Customer not found!");
            return reloadCreatePage(model);
        }

        if (pointsUsed > customer.getPoint()) {
            model.addAttribute("error", "Not enough points!");
            return reloadCreatePage(model);
        }

        Manager manager = managerService.getDefaultManager();
        if (manager == null) {
            model.addAttribute("error", "No manager available!");
            return reloadCreatePage(model);
        }

        Order order = new Order();
        order.setCustomer(customer);
        order.setManager(manager);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus("Pending");
        order.setPointsUsed(pointsUsed);

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

        if (voucher != null) {
            if ("PERCENT".equalsIgnoreCase(voucher.getDiscountType()))
                totalPrice -= totalPrice * (voucher.getDiscountValue() / 100.0);
            else if ("AMOUNT".equalsIgnoreCase(voucher.getDiscountType()))
                totalPrice -= voucher.getDiscountValue();
        }

        totalPrice -= pointsUsed * 2000;
        if (totalPrice < 0) totalPrice = 0;
        order.setTotalPrice(totalPrice);
        order.setOrderItems(orderItems);

        int earnedPoints = (int) (totalPrice / 50000);
        customer.setPoint(customer.getPoint() - pointsUsed + earnedPoints);
        customerService.saveCustomer(customer);

        if (pointsUsed > 0) {
            PointHistory ph = new PointHistory();
            ph.setCustomer(customer);
            ph.setOrder(order);
            ph.setAmount(-pointsUsed);
            ph.setTypeOfChange("Redeemed in order");
            ph.setChangeTime(LocalDateTime.now());
            pointHistoryService.saveHistory(ph);
        }

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
        model.addAttribute("customerList", customerService.getAllCustomers());
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

    @GetMapping("/edit")
    public String showEditOrderPage(
            @RequestParam(defaultValue = "1") int page,
            Model model,
            HttpSession session) {

        int pageSize = 9;
        Page<Order> orderPage = orderService.getPagedOrders(page, pageSize);

        Manager currentUser = (Manager) session.getAttribute("loggedInUser");
        model.addAttribute("currentUser", currentUser);

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("title", "Edit Order");

        return "order/edit";
    }

    @PostMapping("/updateStatus")
    @Transactional
    public String updateOrderStatus(
            @RequestParam("orderId") int orderId,
            @RequestParam("status") String status,
            @RequestParam(value = "updatedAt", required = false) String updatedAtStr,
            HttpSession session) {

        Optional<Order> optionalOrder = orderService.getOrderById(orderId);
        if (optionalOrder.isEmpty()) {
            return "redirect:/order/edit?error=OrderNotFound";
        }

        Order order = optionalOrder.get();
        order.setStatus(status);

        if (updatedAtStr != null && !updatedAtStr.isEmpty()) {
            order.setUpdatedAt(LocalDateTime.parse(updatedAtStr));
        } else {
            order.setUpdatedAt(LocalDateTime.now());
        }

        if ("Completed".equalsIgnoreCase(status) || "Cancelled".equalsIgnoreCase(status)) {
            order.setActive(true);
        } else {
            order.setActive(false);
        }

        orderService.saveOrder(order);
        return "redirect:/order/edit";
    }
}
