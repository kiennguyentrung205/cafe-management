package vn.edu.fpt.cafemanagement.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
}

