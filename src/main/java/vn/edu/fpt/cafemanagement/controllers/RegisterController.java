package vn.edu.fpt.cafemanagement.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import vn.edu.fpt.cafemanagement.entities.Customer;
import vn.edu.fpt.cafemanagement.services.CustomerService;
import vn.edu.fpt.cafemanagement.services.OtpService;
import vn.edu.fpt.cafemanagement.services.PasswordService;
import vn.edu.fpt.cafemanagement.services.RegisterService;

@Controller
public class RegisterController {
    private final RegisterService registerService;
    private OtpService otpService;
    private PasswordService passwordService;

    public RegisterController(RegisterService registerService, OtpService otpService, PasswordService passwordService) {
        this.registerService = registerService;
        this.otpService = otpService;
        this.passwordService = passwordService;
    }

    @GetMapping(path = "/register")
    public String showRegister(Model model){
        model.addAttribute("customer", new Customer());
        return "account/register";
    }


    @PostMapping(path = "/register")
    public String doRegister(Model model, @ModelAttribute Customer customer, HttpSession session){
        String hashPassword = BCrypt.hashpw(customer.getPassword(), BCrypt.gensalt());
        customer.setPassword(hashPassword);
        customer.setImg("avatar.jpeg");
        session.setAttribute("pendingCustomer", customer);

        passwordService.sendOtpForRegister(customer.getEmail());

        // Chuyển hướng sang trang xác thực
        return "redirect:/verify-email";

    }

    @GetMapping("/verify-email")
    public String showVerifyOtp(HttpSession session, Model model) {
        Customer pendingCustomer = (Customer) session.getAttribute("pendingCustomer");

        if (pendingCustomer == null) {
            return "redirect:/register?error=expired";
        }

        model.addAttribute("email", pendingCustomer.getEmail());
        return "account/verify-email";
    }


    @PostMapping("/verify-email")
    public String verifyOtp(String otp,
                            Model model,
                            HttpSession session) {

        Customer pendingCustomer = (Customer) session.getAttribute("pendingCustomer");

        if (pendingCustomer == null) {
            model.addAttribute("errorMessage", "Phiên đăng ký đã hết hạn.");
            return "account/verify-email";
        }

        boolean valid = otpService.validateOtp(pendingCustomer.getEmail(), otp);

        if (!valid) {
            return "redirect:/login?errorMessage=The OTP code is incorrect or has expired!";
        }

        // OTP hợp lệ → lưu vào DB
        try {
            registerService.createCustomer(pendingCustomer);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "account/verify-email";
        }

        // Xóa session tạm
        session.removeAttribute("pendingCustomer");

        return "redirect:/login?successMessage=Your account has been successfully registered. Welcome aboard!";
    }

}
