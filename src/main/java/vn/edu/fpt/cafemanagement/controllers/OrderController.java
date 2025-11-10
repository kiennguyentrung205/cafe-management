package vn.edu.fpt.cafemanagement.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.cafemanagement.entities.*;
import vn.edu.fpt.cafemanagement.security.LoggedUser;
import vn.edu.fpt.cafemanagement.services.*;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/order")
public class OrderController {

    private final ProductService productService;
    private final OrderService orderService;
    private final VoucherService voucherService;
    private final CustomerService customerService;
    private final CategoryService categoryService;
    private final PointHistoryService pointHistoryService;
    private final LoggedUser loggedUser;

    public OrderController(ProductService productService,
                           OrderService orderService,
                           VoucherService voucherService,
                           CustomerService customerService,
                           CategoryService categoryService,
                           PointHistoryService pointHistoryService,
                           LoggedUser loggedUser
    ) {
        this.productService = productService;
        this.orderService = orderService;
        this.voucherService = voucherService;
        this.customerService = customerService;
        this.categoryService = categoryService;
        this.pointHistoryService = pointHistoryService;
        this.loggedUser = loggedUser; // <-- [ĐÃ THÊM]
    }

    // ----------------------- [GET: Hiển thị form tạo order + tìm kiếm + phân trang] -----------------------
    @GetMapping("/create")
    public String showCreateOrderForm(
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "customerPhone", required = false) String customerPhone,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "page", defaultValue = "1") int page,
            Model model, HttpServletRequest request) {

        int pageSize = 15;
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<Product> productPage;

        if ("check".equals(action)) {
            Customer customer = customerService.getCustomerByPhone(customerPhone);
            if (customer == null) {
                model.addAttribute("error", "Customer not found!");
                model.addAttribute("customer", null);
            } else {
                model.addAttribute("customer", customer);
            }
            // Giữ lại SĐT trong ô input sau khi check
            model.addAttribute("customerPhone", customerPhone);
        } else {
            // Trạng thái mặc định khi tải trang
            model.addAttribute("customer", null);
            model.addAttribute("customerPhone", null);
        }

        if (query != null && !query.trim().isEmpty() && categoryId != null && categoryId > 0) {
            // tìm theo cả tên + category
            productPage = productService.searchActiveProductsByCategory(categoryId, query.trim(), pageable);
        } else if (query != null && !query.trim().isEmpty()) {
            // chỉ tìm theo tên
            productPage = productService.searchActiveProducts(query.trim(), pageable);
        } else if (categoryId != null && categoryId > 0) {
            // chỉ lọc theo category
            productPage = productService.getProductsByCategoryPaged(categoryId, pageable);
        } else {
            productPage = productService.getActiveProductsPaged(pageable);
        }

        model.addAttribute("categoryList", categoryService.getCategories());
        model.addAttribute("selectedCategoryId", categoryId != null ? categoryId : 0);
        model.addAttribute("query", query != null ? query : "");
        model.addAttribute("productList", productPage.getContent());
        model.addAttribute("voucherList", voucherService.getActiveVouchers());
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
            @RequestParam(value = "notes", required = false) List<String> notes,
            @RequestParam(value = "voucherId", required = false) Optional<Integer> voucherId,
            @RequestParam(value = "customerPhone", required = false) String customerPhone,
            @RequestParam(value = "pointsUsed", defaultValue = "0") int pointsUsed, // Đây là pointsUsed_Input từ form
            Model model
    ) {

        // --- [CHECK 1] Products (Giữ nguyên) ---
        if (productIds == null || productIds.isEmpty() || "null".equals(String.valueOf(productIds.get(0)))) {
            model.addAttribute("error", "No products selected!");
            return reloadCreatePage(model, null);
        }

        // --- [CHECK 2] Quantities (Giữ nguyên) ---
        if (quantities == null || quantities.size() != productIds.size()) {
            model.addAttribute("error", "Data mismatch between products and quantities. Please refresh and try again.");
            return reloadCreatePage(model, null);
        }

        // --- [CHECK 3] Get Customer (Giữ nguyên) ---
        Customer customer = null;
        if (customerPhone != null && !customerPhone.trim().isEmpty()) {
            customer = customerService.getCustomerByPhone(customerPhone.trim());
            if (customer == null) {
                model.addAttribute("warning", "Customer not found! Order will not be linked to any customer.");
            }
        }

        // --- [CHECK 4] Cannot use both (Giữ nguyên) ---
        Integer voucherIdValue = voucherId.orElse(0);
        if (pointsUsed > 0 && voucherIdValue != 0) {
            model.addAttribute("error", "Cannot use both Voucher and Redeem Points!");
            return reloadCreatePage(model, customer);
        }

        // --- [CHECK 5] Staff check (Giữ nguyên) ---
        Staff staff = loggedUser.getLoggedStaff();
        if (staff == null) {
            model.addAttribute("error", "No logged-in staff! Please login again.");
            return "redirect:/login";
        }

        // --- [STEP 6] Lấy Voucher (Giữ nguyên) ---
        Voucher voucher = null;
        if (voucherIdValue != 0) {
            voucher = voucherService.getVoucherById(voucherIdValue);
            if (voucher == null || voucher.getQuantity() <= 0) {
                model.addAttribute("error", "Voucher invalid or out of stock!");
                return reloadCreatePage(model, customer);
            }
            // *Chưa* giảm số lượng, sẽ giảm ở cuối nếu mọi thứ OK
        }

        // --- [STEP 7] Tính Subtotal (totalPrice) (ĐÃ ĐƯỢC ĐƯA LÊN TRÊN) ---
        double totalPrice = 0; // Subtotal
        List<OrderItem> orderItems = new ArrayList<>();
        for (int i = 0; i < productIds.size(); i++) {
            Product product = productService.getProductById(productIds.get(i));

            if (product == null) {
                model.addAttribute("error", "One of the selected products is invalid or no longer exists!");
                return reloadCreatePage(model, customer);
            }

            int qty = quantities.get(i);
            String note = (notes != null && notes.size() > i) ? notes.get(i) : "";

            OrderItem item = new OrderItem();
            // *Chưa* set Order vội, sẽ set ở cuối
            item.setProduct(product);
            item.setQuantity(qty);
            item.setNote(note);
            orderItems.add(item);
            totalPrice += product.getPrice() * qty;
        }

        // --- [STEP 8] Tính Voucher Discount (ĐÃ ĐƯỢC ĐƯA LÊN TRÊN) ---
        double voucherDiscount = 0;
        if (voucher != null) {
            if ("PERCENT".equalsIgnoreCase(voucher.getDiscountType())) {
                voucherDiscount = Math.ceil(totalPrice * (voucher.getDiscountValue() / 100.0));
            } else if ("AMOUNT".equalsIgnoreCase(voucher.getDiscountType())) {
                voucherDiscount = voucher.getDiscountValue();
            }
        }

        // --- [STEP 9] Tính Price To Pay (ĐÃ ĐƯỢC ĐƯA LÊN TRÊN) ---
        // (Giá trị còn lại PHẢI TRẢ sau khi trừ voucher)
        double priceToPay = totalPrice - voucherDiscount;
        if (priceToPay < 0) priceToPay = 0;

        // --- [STEP 10] Tính Max Points Needed (ĐÃ ĐƯỢC ĐƯA LÊN TRÊN) ---
        // (Số điểm TỐI ĐA CẦN DÙNG cho đơn hàng này, 1 điểm = 1000)
        int maxPointsNeeded = (int) Math.ceil(priceToPay / 1000.0);

        // --- [STEP 11 - NEW VALIDATION] BẮT LỖI pointsUsed TẠI BACKEND ---

        // Validation 1: Check for negative (Kiểm tra số âm)
        if (pointsUsed < 0) {
            model.addAttribute("error", "Points used cannot be negative.");
            return reloadCreatePage(model, customer);
        }

        // Validation 2: Check against what the customer HAS (Kiểm tra điểm khách CÓ)
        // (Chỉ check nếu customer tồn tại và có liên kết)
        if (customer != null && pointsUsed > customer.getPoint()) {
            model.addAttribute("error", "You only have " + customer.getPoint() + " points. Please enter a lower amount.");
            return reloadCreatePage(model, customer);
        }

        // Validation 3: Check against what the order NEEDS (Kiểm tra điểm đơn hàng CẦN)
        if (pointsUsed > maxPointsNeeded) {
            model.addAttribute("error", "This order only requires a maximum of " + maxPointsNeeded + " points. Please enter a lower amount.");
            return reloadCreatePage(model, customer);
        }

        // --- [KẾT THÚC VALIDATION MỚI] ---

        // Nếu qua được 3 bước trên, 'pointsUsed' (từ input) là HỢP LỆ.
        // Chúng ta gán nó là số điểm sẽ dùng thực tế.
        int actualPointsUsed = pointsUsed;

        // --- [STEP 12] TÍNH TOÁN CUỐI CÙNG ---

        // 5. Tính số tiền giảm giá THỰC TẾ (từ điểm)
        double pointsDiscount = actualPointsUsed * 1000.0;

        // 6. Tính giá cuối cùng (sẽ không bao giờ âm)
        double finalPrice = priceToPay - pointsDiscount;
        if (finalPrice < 0) finalPrice = 0;

        // 7. Tính điểm tích lũy (earnedPoints)
        int earnedPoints = (int) (finalPrice / 50000);

        // 8. Làm tròn TỔNG TIỀN CUỐI CÙNG lên 1000 VND
        if (finalPrice > 0) {
            finalPrice = Math.ceil(finalPrice / 1000) * 1000;
        }

        // --- [STEP 13] LƯU VÀO DATABASE ---

        Order order = new Order();
        if (customer != null) order.setCustomer(customer);

        order.setStaff(staff);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setStatus("Pending");
        order.setPointsUsed(actualPointsUsed); // <-- Dùng số điểm đã validate
        order.setTotalPrice(finalPrice);

        // Gắn order vào items
        for(OrderItem item : orderItems) {
            item.setOrder(order);
        }
        order.setOrderItems(orderItems);

        // Gắn voucher và giảm số lượng (nếu có)
        if (voucher != null) {
            order.setVoucher(voucher);
            voucher.setQuantity(voucher.getQuantity() - 1);
            voucherService.saveVoucher(voucher);
        }

        orderService.saveOrder(order); // Lưu order (và items)

        // Cập nhật customer (nếu có)
        if (customer != null) {
            customer.setPoint(customer.getPoint() - actualPointsUsed + earnedPoints);
            customerService.saveCustomer(customer);

            // Ghi lịch sử (Giữ nguyên)
            if (actualPointsUsed > 0) {
                PointHistory ph = new PointHistory();
                ph.setCustomer(customer);
                ph.setOrder(order);
                ph.setAmount(-actualPointsUsed);
                ph.setTypeOfChange("Redeemed in order");
                ph.setChangeTime(LocalDateTime.now());
                pointHistoryService.saveHistory(ph);
            }
            if (earnedPoints > 0) {
                PointHistory phEarned = new PointHistory();
                phEarned.setCustomer(customer);
                phEarned.setOrder(order);
                phEarned.setAmount(earnedPoints);
                phEarned.setTypeOfChange("Earned from order");
                phEarned.setChangeTime(LocalDateTime.now());
                pointHistoryService.saveHistory(phEarned);
            }
        }

        model.addAttribute("success", "Order created successfully!");
        return reloadCreatePage(model, customer);
    }

    // ----------------------- [GET: Danh sách đơn hàng] -----------------------
    @GetMapping("/list")
    public String viewOrders(@RequestParam(defaultValue = "1") int page, Model model) {
        int pageSize = 6;
        Page<Order> orderPage = orderService.getActiveOrders(page, pageSize);

        // [ĐÃ SỬA] Làm tròn giá trước khi gửi sang Thymeleaf
        // (Đảm bảo các order cũ cũng được làm tròn khi hiển thị)
        orderPage.getContent().forEach(order -> {
            double roundedPrice = Math.ceil(order.getTotalPrice() / 1000) * 1000;
            order.setTotalPrice(roundedPrice);
        });

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("customerList", customerService.getAllCustomers());
        return "order/list";
    }

    @GetMapping("/history-list")
    public String viewOrdersHistory(@RequestParam(defaultValue = "1") int page, Model model) {
        int pageSize = 6;
        Page<Order> orderPage = orderService.getHistoryOrders(page, pageSize);

        // [ĐÃ SỬA] Làm tròn giá trước khi gửi sang Thymeleaf
        orderPage.getContent().forEach(order -> {
            double roundedPrice = Math.ceil(order.getTotalPrice() / 1000) * 1000;
            order.setTotalPrice(roundedPrice);
        });

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        return "order/history-list";
    }

    @GetMapping("/edit")
    public String showEditOrderPage(
            @RequestParam(defaultValue = "1") int page,
            Model model
    ) {

        int pageSize = 6;
        // [ĐÃ SỬA] Gọi service method mới để lấy cả Pending và Ready
        Page<Order> orderPage = orderService.getKitchenOrders(page, pageSize);

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("title", "Edit Order");

        return "order/edit";
    }

    @GetMapping("/edit-history")
    public String showEditOrderHistoryPage(
            @RequestParam(defaultValue = "1") int page,
            Model model
    ) {

        int pageSize = 6;
        Page<Order> orderPage = orderService.getServedOrCanceledOrders(page, pageSize);

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("title", "Edit Order History");

        return "order/edit-history";
    }

    // ----------------------- [ĐÃ SỬA LỖI HOÀN CHỈNH] -----------------------
    @PostMapping("/updateStatus")
    @Transactional
    public String updateOrderStatus(
            @RequestParam("orderId") int orderId,
            @RequestParam("status") String status
    ) {

        // 1. Kiểm tra nếu Barista quên chọn (status là rỗng)
        if (status == null || status.trim().isEmpty()) {
            return "redirect:/order/edit";
        }

        // 2. Xử lý "Canceled"
        if ("Canceled".equals(status)) {
            Staff currentUser = loggedUser.getLoggedStaff();

            // --- DEBUG ---
            // In ra Role thực tế của user trong CONSOLE
            System.out.println("DEBUG: User role is: '" + currentUser.getRole() + "'");
            // --- HẾT DEBUG ---

            // [SỬA LỖI] Phải so sánh với "ROLE_BARISTA"
            if (currentUser == null || !currentUser.getRole().equals("ROLE_BARISTA")) {
                System.out.println("DEBUG: Cancel FAILED role check."); // Thêm debug
                return "redirect:/order/edit?error=UnauthorizedCancel";
            }

            try {
                orderService.deleteOrderById(orderId);
            } catch (Exception e) {
                return "redirect:/order/edit?error=DeleteFailed";
            }
            return "redirect:/order/edit?success=OrderCanceled";
        }

        // 3. Xử lý "Ready" và "Served"
        Staff currentUser = loggedUser.getLoggedStaff();
        if (currentUser == null) {
            return "redirect:/login";
        }

        // --- DEBUG ---
        // In ra Role thực tế của user trong CONSOLE
        System.out.println("DEBUG: User role is: '" + currentUser.getRole() + "'");
        // --- HẾT DEBUG ---

        Optional<Order> optionalOrder = orderService.getOrderById(orderId);
        if (optionalOrder.isEmpty()) {
            return "redirect:/order/edit?error=OrderNotFound";
        }

        Order order = optionalOrder.get();

        // [SỬA LỖI] Phải so sánh với "ROLE_BARISTA"
        if (status.equals("Ready") && !currentUser.getRole().equals("ROLE_BARISTA")) {
            System.out.println("DEBUG: Ready FAILED role check."); // Thêm debug
            return "redirect:/order/edit?error=UnauthorizedReady";
        }

        // [SỬA LỖI] Phải so sánh với "ROLE_WAITER"
        if (status.equals("Served") && !currentUser.getRole().equals("ROLE_WAITER")) {
            System.out.println("DEBUG: Served FAILED role check."); // Thêm debug
            return "redirect:/order/edit?error=UnauthorizedServed";
        }

        // --- Chỉ khi qua được các bước trên, code mới chạy tới đây ---
        System.out.println("DEBUG: Validation PASSED. Saving status: " + status); // Thêm debug
        order.setStatus(status);
        orderService.updateOrder(order, currentUser);

        return "redirect:/order/edit";
    }

    @GetMapping("/detail/{id}")
    @ResponseBody
    public Map<String, Object> getOrderDetail(@PathVariable("id") int orderId) {
        Map<String, Object> response = new HashMap<>();

        Optional<Order> optionalOrder = orderService.getOrderById(orderId);
        if (optionalOrder.isEmpty()) {
            response.put("success", false);
            response.put("message", "Order not found!");
            return response;
        }

        Order order = optionalOrder.get();
        List<OrderItem> items = orderService.getOrderItemsByOrderId(orderId);

        // Lấy customer ra và kiểm tra null
        Customer customer = order.getCustomer();

        double roundedPrice = Math.ceil(order.getTotalPrice() / 1000) * 1000;

        response.put("success", true);
        response.put("order", Map.of(
                "id", order.getOrderId(),
                // Kiểm tra null
                "customer", customer != null ? customer.getName() : "N/A",
                "staff", order.getStaff() != null ? order.getStaff().getName() : "N/A",
                "status", order.getStatus(),
                "pointsUsed", order.getPointsUsed(),
                "voucher", order.getVoucher() != null ? order.getVoucher().getVoucherName() : "None",
                "totalPrice", roundedPrice,
                "date", order.getCreatedAt(),
                "update", order.getUpdatedAt() != null ? order.getUpdatedAt() : "-",
                "products", items.stream()
                        .map(i -> Map.of(
                                "name", i.getProduct().getProName(),
                                "price", i.getProduct().getPrice(),
                                "quantity", i.getQuantity()
                        ))
                        .toList()
        ));

        // Kiểm tra null cho toàn bộ
        if (customer != null) {
            // SỬA LỖI: Dùng HashMap thay vì Map.of()
            // HashMap cho phép giá trị (value) là null
            Map<String, Object> customerMap = new HashMap<>();
            customerMap.put("id", customer.getCusId());
            customerMap.put("name", customer.getName());
            customerMap.put("phone", customer.getPhoneNumber()); // Sẽ là null nếu DB là null
            customerMap.put("email", customer.getEmail());       // Sẽ là null nếu DB là null
            customerMap.put("address", customer.getAddress());   // Sẽ là null nếu DB là null
            customerMap.put("img", customer.getImg());         // Sẽ là null nếu DB là null

            response.put("customer", customerMap);

        } else {
            // Trả về một đối tượng customer rỗng nếu không có
            // Khối 'else' này của bạn đã đúng, không cần sửa
            response.put("customer", Map.of(
                    "id", "N/A",
                    "name", "N/A",
                    "phone", "N/A",
                    "email", "N/A",
                    "address", "",
                    "img", ""
            ));
        }

        return response;
    }


    private String reloadCreatePage(Model model, Customer customer) {
        model.addAttribute("categoryList", categoryService.getCategories());

        // Tải lại product list với phân trang cơ bản
        Page<Product> productPage = productService.getActiveProductsPaged(PageRequest.of(0, 10));
        model.addAttribute("productList", productPage.getContent());
        model.addAttribute("currentPage", 1);
        model.addAttribute("totalPages", productPage.getTotalPages());

        model.addAttribute("voucherList", voucherService.getActiveVouchers());

        // Giữ lại customer (dù là null hay không)
        model.addAttribute("customer", customer);

        // Giữ lại SĐT nếu có
        if (customer != null) {
            model.addAttribute("customerPhone", customer.getPhoneNumber());
        } else {
            model.addAttribute("customerPhone", null);
        }

        model.addAttribute("selectedCategoryId", 0);
        model.addAttribute("query", "");
        model.addAttribute("title", "Create In-Store Order");

        return "order/create";
    }
}