package vn.edu.fpt.cafemanagement.controllers;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import vn.edu.fpt.cafemanagement.entities.Customer;
import vn.edu.fpt.cafemanagement.services.CustomerService;

@Controller
public class RegisterController {
    private final CustomerService customerService;

    public RegisterController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping(path = "/register")
    public String showRegister(Model model){
        model.addAttribute("customer", new Customer());
        return "account/register";
    }

    @PostMapping(path = "/register")
    public String register(Model model, @ModelAttribute Customer customer){
        String hashPassword = BCrypt.hashpw(customer.getPassword(), BCrypt.gensalt());
        customer.setPassword(hashPassword);

        try {
            customerService.createCustomer(customer);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "account/register";
        }
        return "redirect:/login?sucess";
    }

}
