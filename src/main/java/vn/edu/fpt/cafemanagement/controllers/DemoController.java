package vn.edu.fpt.cafemanagement.controllers;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DemoController {
    @GetMapping("/home")
    public String home(Model model, Authentication authentication) {
        model.addAttribute("name", authentication.getName());
//        model.addAttribute("role", authentication.getPrincipal().ge);
        return "home/home";
    }
}
