package vn.edu.fpt.cafemanagement.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.cafemanagement.dto.OrderRequestDTO;
import vn.edu.fpt.cafemanagement.entities.Customer;
import vn.edu.fpt.cafemanagement.entities.Order;
import vn.edu.fpt.cafemanagement.repositories.*;
import vn.edu.fpt.cafemanagement.services.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ManagerRepository managerRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    // Hiển thị form tạo đơn hàng
    @GetMapping("/create")
    public String showCreateOrderForm(Model model) {
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("managers", managerRepository.findAll());
        model.addAttribute("vouchers", voucherRepository.findAll());
        model.addAttribute("orderRequest", new OrderRequestDTO());
        return "order/create";
    }

    // Xử lý khi submit form
    @PostMapping("/create")
    public String createInStoreOrder(@ModelAttribute("orderRequest") OrderRequestDTO orderRequest,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.createInStoreOrder(orderRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Order created successfully! Order ID: " + order.getOrderId());
            return "redirect:/orders/success";
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("products", productRepository.findAll());
            model.addAttribute("managers", managerRepository.findAll());
            model.addAttribute("vouchers", voucherRepository.findAll());
            return "order/create";
        }
    }

    // Tìm customer theo phone (AJAX)
    @GetMapping(value = "/find-customer", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> findCustomerByPhone(@RequestParam("phone") String phone) {
        Optional<Customer> opt = customerRepository.findByPhoneNumber(phone);
        if (opt.isPresent()) {
            Customer c = opt.get();
            Map<String, Object> res = new HashMap<>();
            res.put("cusId", c.getCusId());
            res.put("username", c.getUsername());
            res.put("point", c.getPoint());
            return ResponseEntity.ok(res);
        } else {
            return ResponseEntity.ok(Map.of("notFound", true));
        }
    }

    // Page success
    @GetMapping("/success")
    public String showSuccessPage() {
        return "order/success";
    }
}
