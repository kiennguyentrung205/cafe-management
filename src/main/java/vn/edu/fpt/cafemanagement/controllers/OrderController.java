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
            @RequestParam(value = "pointsUsed", defaultValue = "0") int pointsUsed,
            Model model
    ) {

        if (productIds == null || productIds.isEmpty() || "null".equals(String.valueOf(productIds.get(0)))) {
            model.addAttribute("error", "No products selected!");
            return reloadCreatePage(model, null);
        }

        if (quantities == null || quantities.size() != productIds.size()) {
            model.addAttribute("error", "Data mismatch between products and quantities. Please refresh and try again.");
            return reloadCreatePage(model, null);
        }

        Customer customer = null;
        if (customerPhone != null && !customerPhone.trim().isEmpty()) {
            customer = customerService.getCustomerByPhone(customerPhone.trim());
            if (customer == null) {
                model.addAttribute("warning", "Customer not found! Order will not be linked to any customer.");
            }
        }

        if (customer != null && pointsUsed > customer.getPoint()) {
            model.addAttribute("error", "Not enough points!");
            return reloadCreatePage(model, customer);
        }

        Integer voucherIdValue = voucherId.orElse(0);
        if (pointsUsed > 0 && voucherIdValue != 0) {
            model.addAttribute("error", "Cannot use both Voucher and Redeem Points!");
            return reloadCreatePage(model, customer);
        }

        Staff staff = loggedUser.getLoggedStaff();
        if (staff == null) {
            model.addAttribute("error", "No logged-in staff! Please login again.");
            // Chuyển về trang login nếu không tìm thấy user
            return "redirect:/login";
        }

        Order order = new Order();
        if (customer != null) order.setCustomer(customer);

        order.setStaff(staff);      // người tạo đơn
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setStatus("Pending");
        order.setPointsUsed(pointsUsed);

        Voucher voucher = null;
        if (voucherIdValue != 0) {
            voucher = voucherService.getVoucherById(voucherIdValue);
            if (voucher == null || voucher.getQuantity() <= 0) {
                model.addAttribute("error", "Voucher invalid or out of stock!");
                return reloadCreatePage(model, customer);
            }
            order.setVoucher(voucher);
            voucher.setQuantity(voucher.getQuantity() - 1);
            voucherService.saveVoucher(voucher);
        }

        double totalPrice = 0; // Đây là Subtotal
        List<OrderItem> orderItems = new ArrayList<>();
        for (int i = 0; i < productIds.size(); i++) {
            Product product = productService.getProductById(productIds.get(i));

            if (product == null) {
                model.addAttribute("error", "One of the selected products is invalid or no longer exists!");
                // Phải nạp lại model và customer (nếu có)
                return reloadCreatePage(model, customer);
            }

            int qty = quantities.get(i);
            String note = (notes != null && notes.size() > i) ? notes.get(i) : "";


            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(qty);
            item.setNote(note);
            orderItems.add(item);
            totalPrice += product.getPrice() * qty;
        }

        double voucherDiscount = 0;
        if (voucher != null) {
            if ("PERCENT".equalsIgnoreCase(voucher.getDiscountType())) {
                // [SỬA 1] Làm tròn tiền discount (theo %_giam_gia)
                voucherDiscount = Math.ceil(totalPrice * (voucher.getDiscountValue() / 100.0));
            } else if ("AMOUNT".equalsIgnoreCase(voucher.getDiscountType())) {
                voucherDiscount = voucher.getDiscountValue();
            }
        }

        // 1. Tính giá trị còn lại PHẢI TRẢ (sau khi trừ voucher)
        double priceToPay = totalPrice - voucherDiscount;
        if (priceToPay < 0) priceToPay = 0;

        // 2. Tính số điểm TỐI ĐA CẦN DÙNG (1 điểm = 2000đ)
        // (Ví dụ: 18.000đ / 2000 = 9 điểm)
        // (Ví dụ: 16.000đ / 2000 = 8 điểm)
        int maxPointsNeeded = (int) Math.ceil(priceToPay / 2000.0);

        // 3. Lấy số điểm khách MUỐN DÙNG (từ form, là bội số của 5)
        // (Ví dụ: 10 điểm)
        int pointsUsed_Input = pointsUsed;

        // 4. Tính số điểm THỰC TẾ SẼ TRỪ
        // (Là số nhỏ hơn: số khách CHỌN hoặc số CẦN)
        // (Ví dụ: min(10, 9) = 9 điểm)
        // (Ví dụ: min(10, 8) = 8 điểm)
        int actualPointsUsed = Math.min(pointsUsed_Input, maxPointsNeeded);

        // 5. Tính số tiền giảm giá THỰC TẾ (e.g., 9 * 2000 = 18000)
        double pointsDiscount = actualPointsUsed * 2000.0;

        // 6. Tính giá cuối cùng (sẽ không bao giờ âm)
        double finalPrice = priceToPay - pointsDiscount;
        if (finalPrice < 0) finalPrice = 0;

        // 7. Tính điểm tích lũy (earnedPoints)
        // (Tính trên giá sau khi đã trừ điểm)
        int earnedPoints = (int) (finalPrice / 50000);

        // [SỬA 2] Làm tròn TỔNG TIỀN CUỐI CÙNG lên 1000 VND
        if (finalPrice > 0) {
            finalPrice = Math.ceil(finalPrice / 1000) * 1000;
        }

        //Làm tròn TỔNG TIỀN CUỐI CÙNG lên 1000 VND
        if (finalPrice > 0) {
            finalPrice = Math.ceil(finalPrice / 1000) * 1000;
        }

        order.setTotalPrice(finalPrice);
        order.setOrderItems(orderItems);
        // ------------------------------------------

        orderService.saveOrder(order);

        if (customer != null) {
            customer.setPoint(customer.getPoint() - actualPointsUsed + earnedPoints);
            customerService.saveCustomer(customer);

            // 1. GHI LỊCH SỬ NẾU DÙNG ĐIỂM
            if (actualPointsUsed > 0) {
                PointHistory ph = new PointHistory();
                ph.setCustomer(customer);
                ph.setOrder(order);
                ph.setAmount(-actualPointsUsed); // <-- Dùng actualPointsUsed
                ph.setTypeOfChange("Redeemed in order");
                ph.setChangeTime(LocalDateTime.now());
                pointHistoryService.saveHistory(ph);
            }

            // 2. GHI LỊCH SỬ NẾU NHẬN ĐIỂM
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
        Page<Order> orderPage = orderService.getUnservedOrders(page, pageSize);

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

        // 1. Lấy Staff (Barista) đang đăng nhập từ LoggedUser service
        Staff currentUser = loggedUser.getLoggedStaff();
        if (currentUser == null) {
            // Nếu không có ai đăng nhập, chuyển về trang login
            return "redirect:/login";
        }

        // 2. Lấy Order
        Optional<Order> optionalOrder = orderService.getOrderById(orderId);
        if (optionalOrder.isEmpty()) {
            return "redirect:/order/edit?error=OrderNotFound";
        }

        Order order = optionalOrder.get();
        // 3. Cập nhật trạng thái
        order.setStatus(status);

        // 4. Gọi phương thức service ĐÚNG
        // Phương thức này sẽ tự động set 'updatedBy = currentUser' và 'updatedAt = now()'
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