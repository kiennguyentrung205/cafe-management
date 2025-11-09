package vn.edu.fpt.cafemanagement.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.fpt.cafemanagement.entities.Customer;
import vn.edu.fpt.cafemanagement.entities.PointHistory;
import vn.edu.fpt.cafemanagement.security.LoggedUser;
import vn.edu.fpt.cafemanagement.services.CustomerService;

import java.util.List;

@Controller
@RequestMapping(value = "/profile")
public class CustomerController {
    @Autowired
    private CustomerService customerService;
    @Autowired
    private LoggedUser loggedUser;

    @RequestMapping(value = "")
    public String viewProfile(Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CUSTOMER"))) {
            return "redirect:/login";
        }
        Customer sessionCustomer = loggedUser.getLoggedCustomer();
        if (sessionCustomer == null) {
            return "redirect:/login";
        }
        Customer customer = customerService.getCustomerById(sessionCustomer.getCusId());
        model.addAttribute("customer", customer);
        return "profile/view";
    }

    @RequestMapping(value = "/pointhistory")
    public String viewPointHistory(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CUSTOMER"))) {
            return "redirect:/login";
        }
        Customer sessionCustomer = loggedUser.getLoggedCustomer();
        if (sessionCustomer == null) {
            return "redirect:/login";
        }

        int cusId = sessionCustomer.getCusId();
        List<PointHistory> pointHistoryList = customerService.getPointHistoryByCustomerId(cusId);
        model.addAttribute("pointHistoryList", pointHistoryList);
        return "profile/pointHistory";
    }

    @GetMapping("/edit")
    public String editProfile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CUSTOMER"))) {
            return "redirect:/login";
        }
        Customer sessionCustomer = loggedUser.getLoggedCustomer();
        if (sessionCustomer == null) {
            return "redirect:/login";
        }
        Customer customer = customerService.getCustomerById(sessionCustomer.getCusId());
        model.addAttribute("customer", customer);
        return "profile/edit";
    }

    @PostMapping("/edit")
    public String editProfile(@ModelAttribute(name = "customer") Customer customer, @RequestParam(value = "imgFile") MultipartFile imgFile, Model model) {
        try {
            if (customer.getPhoneNumber() == null || customer.getPhoneNumber().isEmpty()) {
                return "redirect:/profile/edit";
            }
            customerService.updateCustomer(customer, imgFile);
            return "redirect:/profile";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("customer", customer);
            return "profile/edit";
        }
    }

    @RequestMapping(value = "/changePassword")
    public String changePassword(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CUSTOMER"))) {
            return "redirect:/login";
        }
        Customer customer = loggedUser.getLoggedCustomer();
        if (customer == null) {
            return "redirect:/login";
        }
        model.addAttribute("customer", customer);
        return "profile/changePassword";
    }

    @PostMapping("/changePassword")
    public String changePassword(@ModelAttribute(name = "customer") Customer customer, @RequestParam(value = "currentPassword") String currentPassword, @RequestParam(value = "newPassword") String newPassword, @RequestParam(value = "confirmPassword") String confirmPassword, Model model) {
        try {
            customerService.changePassword(customer.getCusId(), newPassword, confirmPassword, currentPassword);
            return "redirect:/profile";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("customer", customer);
            return "profile/changePassword";
        }
    }


}
