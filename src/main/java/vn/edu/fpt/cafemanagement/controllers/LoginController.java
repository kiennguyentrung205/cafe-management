package vn.edu.fpt.cafemanagement.controllers;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping(path={"/customer/login","/login"})
    public String customerLogin() {
        return  "account/customerLogin";
    }

    @GetMapping(path="/staff/login")
    public String staffLogin() {
        return  "account/staffLogin";
    }
}
