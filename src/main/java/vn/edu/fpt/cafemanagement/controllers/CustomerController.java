package vn.edu.fpt.cafemanagement.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.fpt.cafemanagement.entities.Customer;
import vn.edu.fpt.cafemanagement.entities.PointHistory;
import vn.edu.fpt.cafemanagement.services.CustomerService;

import java.util.List;

@Controller
@RequestMapping(value = "/customer")
public class CustomerController {
    @Autowired
    CustomerService customerService;

    @RequestMapping(value = "/profile/{id}")
    public String viewProfile(@PathVariable("id") int cusId, Model model) {
        Customer customer = customerService.getCustomerById(cusId);
        System.out.println("Customer: " + cusId);
        model.addAttribute("customer", customer);
        return "profile/view";
    }

    @RequestMapping(value = "/profile/pointhistory/{id}")
    public String viewPointHistory(@PathVariable("id") int cusId, Model model) {
        List<PointHistory> pointHistoryList = customerService.getPointHistoryByCustomerId(cusId);
        model.addAttribute("pointHistoryList", pointHistoryList);
        return "profile/pointHistory";
    }

    @RequestMapping(value = "/profile/edit/{id}")
    public String editProfile(@PathVariable("id") int cusId, Model model) {
        Customer customer = customerService.getCustomerById(cusId);
        model.addAttribute("customer", customer);
        return "profile/edit";
    }

    @PostMapping("/profile/edit")
    public String editProfile(@ModelAttribute(name = "customer") Customer customer,
                              @RequestParam(value = "imgFile") MultipartFile imgFile , Model model) {
        try {
            if(customer.getPhoneNumber() == null || customer.getPhoneNumber().isEmpty()) {
                return "redirect:/customer/profile/edit/" + customer.getCusId();
            }
            customerService.updateCustomer(customer, imgFile);
            return "redirect:/customer/profile/" + customer.getCusId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("customer", customer);
            return "profile/edit";
        }
    }

    @RequestMapping(value = "/profile/changePassword/{id}")
    public String changePassword(@PathVariable("id") int cusId, Model model) {
        Customer customer = customerService.getCustomerById(cusId);
        model.addAttribute("customer", customer);
        return "profile/changePassword";
    }

    @PostMapping("/profile/changePassword")
    public String changePassword(@ModelAttribute(name = "customer") Customer customer,
                                 @RequestParam(value = "currentPassword") String currentPassword,
                                 @RequestParam(value = "newPassword") String newPassword,
                                 @RequestParam(value = "confirmPassword") String confirmPassword,
                                 Model model) {
        try {
            customerService.changePassword(customer.getCusId(), newPassword, confirmPassword, currentPassword);
            return "redirect:/customer/profile/" + customer.getCusId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("customer", customer);
            return "profile/changePassword";
        }
    }


}
