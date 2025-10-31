package vn.edu.fpt.cafemanagement.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.fpt.cafemanagement.services.PasswordService;

@Controller
public class ForgotPasswordController {

    @Autowired
    private PasswordService passwordService;

    @RequestMapping(value = "/forgot-password")
    public String forgotPassword() {
        return "account/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email, Model model) {
        try {
            passwordService.sendOtpToEmail(email);
            return "redirect:/set-password?email=" + email;
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "account/forgot-password";
        }
    }

    @RequestMapping(value = "/set-password")
    public String setPassword(@RequestParam(required = false) String email, Model model) {
        model.addAttribute("email", email);
        return "account/set-password";
    }

    @PostMapping("/set-password")
    public String setPassword(@RequestParam String email,
                              @RequestParam String otp,
                              @RequestParam String newPassword,
                              @RequestParam String confirmPassword,
                              Model model) {
        try {
            passwordService.resetPassword(email, otp, newPassword, confirmPassword);
            return "redirect:/login?resetSuccess=true"; //Chuyen ve trang login
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("email", email);
            return "account/set-password";
        }
    }
}

