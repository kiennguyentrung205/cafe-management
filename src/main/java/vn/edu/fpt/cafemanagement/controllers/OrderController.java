package vn.edu.fpt.cafemanagement.controllers;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
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
    private final PointHistoryService pointHistoryService;

    public OrderController(ProductService productService,
                           OrderService orderService,
                           VoucherService voucherService,
                           CustomerService customerService,
                           ManagerService managerService,
                           PointHistoryService pointHistoryService) {
        this.productService = productService;
        this.orderService = orderService;
        this.voucherService = voucherService;
        this.customerService = customerService;
        this.managerService = managerService;
        this.pointHistoryService = pointHistoryService;
    }

    // ----------------------- [GET: Hiển thị form tạo order] -----------------------
    @GetMapping("/create")
    public String showCreateOrderForm(@RequestParam(defaultValue = "1") int page, Model model) {
        int size = 6; // mỗi trang 6 sản phẩm
        Page<Product> productPage = productService.getActiveProductsPaged(page, size);

        model.addAttribute("title", "Create In-Store Order");
        model.addAttribute("productList", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("voucherList", voucherService.getActiveVouchers());
        model.addAttribute("customer", null);

        return "order/create";
    }

    // ----------------------- [POST: Tạo order mới] -----------------------
    @PostMapping("/create")
    public String createOrder(
            @RequestParam("productIds") List<Integer> productIds,
            @RequestParam("quantities") List<Integer> quantities,
            @RequestParam(value = "voucherId", required = false) Optional<Integer> voucherId,
            @RequestParam("customerPhone") String customerPhone,
            @RequestParam(value = "pointsUsed", defaultValue = "0") int pointsUsed,
            Model model) {

        // --- Lấy thông tin khách hàng ---
        Customer customer = customerService.getCustomerByPhone(customerPhone);
        if (customer == null) {
            model.addAttribute("error", "Customer not found!");
            model.addAttribute("productList", productService.getActiveProducts());
            model.addAttribute("voucherList", voucherService.getActiveVouchers());
            return "order/create";
        }

        // --- Kiểm tra manager ---
        Manager manager = managerService.getDefaultManager();
        if (manager == null) {
            model.addAttribute("error", "No manager available! Please check manager data.");
            model.addAttribute("productList", productService.getActiveProducts());
            model.addAttribute("voucherList", voucherService.getActiveVouchers());
            return "order/create";
        }

        // --- Tạo đối tượng Order ---
        Order order = new Order();
        order.setCustomer(customer);
        order.setManager(manager);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus("Pending");
        order.setPointsUsed(pointsUsed);

        // --- Áp dụng voucher nếu có ---
        Voucher voucher = null;
        if (voucherId.isPresent() && voucherId.get() != 0) {
            voucher = voucherService.getVoucherById(voucherId.get());
            if (voucher != null && voucher.getQuantity() > 0) {
                order.setVoucher(voucher);

                // Giảm số lượng voucher còn lại
                voucher.setQuantity(voucher.getQuantity() - 1);
                voucherService.saveVoucher(voucher);
            } else {
                model.addAttribute("error", "Voucher is invalid or out of stock!");
                model.addAttribute("productList", productService.getActiveProducts());
                model.addAttribute("voucherList", voucherService.getActiveVouchers());
                return "order/create";
            }
        }

        // --- Tính tổng tiền và danh sách sản phẩm ---
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

        // --- Áp dụng giảm giá nếu có voucher ---
        if (voucher != null) {
            if ("PERCENT".equalsIgnoreCase(voucher.getDiscountType())) {
                totalPrice -= totalPrice * (voucher.getDiscountValue() / 100.0);
            } else if ("AMOUNT".equalsIgnoreCase(voucher.getDiscountType())) {
                totalPrice -= voucher.getDiscountValue();
            }
        }

        // --- Trừ điểm đổi thưởng ---
        totalPrice -= pointsUsed;
        if (totalPrice < 0) totalPrice = 0;
        order.setTotalPrice(totalPrice);
        order.setOrderItems(orderItems);

        // --- Cập nhật điểm khách hàng ---
        int newPoint = customer.getPoint() - pointsUsed + (int) (totalPrice / 1000); // 1 điểm / 1000 VND
        customer.setPoint(newPoint);
        customerService.saveCustomer(customer);

        // --- Ghi lịch sử điểm nếu có sử dụng ---
        if (pointsUsed > 0) {
            PointHistory ph = new PointHistory();
            ph.setCustomer(customer);
            ph.setOrder(order);
            ph.setAmount(pointsUsed * -1);
            ph.setTypeOfChange("Redeemed in order");
            ph.setChangeTime(LocalDateTime.now());
            pointHistoryService.saveHistory(ph);
        }

        // --- Lưu Order vào DB ---
        orderService.saveOrder(order);

        // --- Gửi phản hồi về view ---
        model.addAttribute("success", "Order created successfully!");
        model.addAttribute("order", order);
        return "order/success";
    }

    // ----------------------- [GET: Danh sách đơn hàng] -----------------------
    @GetMapping("/list")
    public String viewOrders(@RequestParam(defaultValue = "1") int page, Model model) {
        int pageSize = 6;
        Page<Order> orderPage = orderService.getPagedOrders(page, pageSize);

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());

        return "order/list";
    }
}
