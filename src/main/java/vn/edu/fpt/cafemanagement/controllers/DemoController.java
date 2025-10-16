package vn.edu.fpt.cafemanagement.controllers;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.edu.fpt.cafemanagement.entities.Customer;
import vn.edu.fpt.cafemanagement.security.LoggedUser;

@Controller
public class DemoController {
    private final LoggedUser loggedUser;

    public DemoController(LoggedUser loggedUser) {
        this.loggedUser = loggedUser;
    }

    @GetMapping("/home")
    public String home(Model model, Authentication authentication) {
        Customer customer= loggedUser.getLoggedCustomer();

        if(customer.getDateOfBirth() == null || customer.getPhoneNumber() == null) {
            return "redirect:/customer/profile/edit/" + customer.getCusId();
        }
//        if(customer.getDateOfBirth() == null){
//            model.addAttribute("completeInfo", true);
//        }

        model.addAttribute("name", customer.getName());
//        model.addAttribute("role", authentication.getPrincipal().ge);
        return "home/home";
    }
}
