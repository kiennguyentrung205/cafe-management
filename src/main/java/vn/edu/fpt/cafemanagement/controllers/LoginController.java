package vn.edu.fpt.cafemanagement.controllers;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {
//    @GetMapping(path="/login")
//    public String login() {
//        return "account/login";
//    }

    @GetMapping(path={"/customer/login","/login"})
    public String customerLogin(Model model,
                                @RequestParam(value = "error", required = false) boolean error) {
        if(error){
            model.addAttribute("error", "Invalid username or password");
        }
        return  "account/customerLogin";
    }

    @GetMapping(path="/staff/login")
    public String staffLogin(Model model,
                             @RequestParam(value = "error", required = false) boolean error) {
        if(error){
            model.addAttribute("error", "Invalid username or password");
        }
        return  "account/staffLogin";
    }
}
