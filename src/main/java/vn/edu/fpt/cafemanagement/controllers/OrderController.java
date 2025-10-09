package vn.edu.fpt.cafemanagement.controllers;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.*;
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

    private ProductService productService;
    private OrderService orderService;
    private VoucherService voucherService;
    private CustomerService customerService;
    private ManagerService managerService;

    public OrderController(ProductService productService,
                           OrderService orderService,
                           VoucherService voucherService,
                           CustomerService customerService,
                           ManagerService managerService) {
        this.productService = productService;
        this.orderService = orderService;
        this.voucherService = voucherService;
        this.customerService = customerService;
        this.managerService = managerService;
    }

    @GetMapping("/create")
    public String showCreateOrderForm(@RequestParam(defaultValue = "1") int page,
                                      Model model) {
        int size = 6; //mỗi trang 6 sản phẩm
        Page<Product> productPage = productService.getActiveProductsPaged(page, size);

        model.addAttribute("title", "Create In-Store Order");
        model.addAttribute("productList", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("voucherList", voucherService.getActiveVouchers());
        model.addAttribute("customer", null);

        return "order/create";
    }

    @PostMapping("/create")
    public String createOrder(
            @RequestParam("productIds") List<Integer> productIds,
            @RequestParam("quantities") List<Integer> quantities,
            @RequestParam(value = "voucherId", required = false) Optional<Integer> voucherId,
            @RequestParam("customerPhone") String customerPhone,
            @RequestParam(value = "pointsUsed", defaultValue = "0") int pointsUsed,
            Model model) {

        //Lấy thông tin khách hàng theo SĐT
        Customer customer = customerService.getCustomerByPhone(customerPhone);
        if (customer == null) {
            model.addAttribute("error", "Customer not found!");
            model.addAttribute("productList", productService.getActiveProducts());
            model.addAttribute("voucherList", voucherService.getActiveVouchers());
            return "order/create";
        }

        //Tạo đối tượng Order mới
        Order order = new Order();
        order.setCustomer(customer);
        order.setManager(managerService.getDefaultManager()); // cashier đang đăng nhập
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus("Pending");
        order.setPointsUsed(pointsUsed);

        //Áp dụng voucher nếu có
        Voucher voucher = null;
        if (voucherId.isPresent() && voucherId.get() != 0) {
            voucher = voucherService.getVoucherById(voucherId.get());
            order.setVoucher(voucher);
        }

        //Tính tổng tiền và danh sách OrderItem
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

        //Giảm giá nếu có voucher
        if (voucher != null) {
            if ("PERCENT".equalsIgnoreCase(voucher.getDiscountType())) {
                totalPrice -= totalPrice * (voucher.getDiscountValue() / 100.0);
            } else if ("AMOUNT".equalsIgnoreCase(voucher.getDiscountType())) {
                totalPrice -= voucher.getDiscountValue();
            }
        }

        //Trừ điểm đổi thưởng
        totalPrice -= pointsUsed;
        if (totalPrice < 0) totalPrice = 0;

        order.setOrderItems(orderItems);
        order.setTotalPrice(totalPrice);

        //Lưu vào DB
        orderService.saveOrder(order);

        //Cập nhật điểm khách hàng
        int newPoint = customer.getPoint() - pointsUsed + (int) (totalPrice / 1000); // tích 1 điểm/1000 VND
        customer.setPoint(newPoint);
        customerService.saveCustomer(customer);

        //Ghi lịch sử điểm
        PointHistory ph = new PointHistory();
        ph.setCustomer(customer);
        ph.setOrder(order);
        ph.setAmount(pointsUsed * -1); // trừ điểm
        ph.setTypeOfChange("Redeemed in order");
        ph.setChangeTime(LocalDateTime.now());
        order.setPointHistories(List.of(ph));

        orderService.saveOrder(order);

        //Gửi lại kết quả
        model.addAttribute("success", "Order created successfully!");
        model.addAttribute("order", order);
        return "order/success";
    }

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
