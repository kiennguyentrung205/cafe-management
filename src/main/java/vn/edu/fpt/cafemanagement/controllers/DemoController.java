package vn.edu.fpt.cafemanagement.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.edu.fpt.cafemanagement.security.models.CustomUserDetails;

@Controller
public class DemoController {
    @GetMapping("/home")
    public String home(Model model, Authentication authentication) {
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        if (user.isCustomer()) {
            model.addAttribute("customer", user.getCustomer());
        } else {
            model.addAttribute("manager", user.getManager());
        }
        model.addAttribute("user", user);
        return "home";
    }
}
