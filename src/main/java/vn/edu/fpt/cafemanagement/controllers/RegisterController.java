package vn.edu.fpt.cafemanagement.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RegisterController {
    @GetMapping(path = "/register")
    public String showRegister(){
        return "account/register";
    }
}
