package vn.edu.fpt.cafemanagement.controllers;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.edu.fpt.cafemanagement.entities.Customer;
import vn.edu.fpt.cafemanagement.entities.Staff;
import vn.edu.fpt.cafemanagement.security.LoggedUser;

@Controller
public class DemoController {
    private final LoggedUser loggedUser;

    public DemoController(LoggedUser loggedUser) {
        this.loggedUser = loggedUser;
    }

    @GetMapping("/home")
    public String home(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("role " + auth.getAuthorities());

        if(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CUSTOMER"))) {
            Customer customer= loggedUser.getLoggedCustomer();

            if(customer.getPhoneNumber() == null) {
                return "redirect:/customer/profile/edit/" + customer.getCusId();
            }

            model.addAttribute("name", customer.getName());
        }

        if(!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CUSTOMER"))) {
            Staff staff = loggedUser.getLoggedStaff();
            System.out.println(staff.getName());
            model.addAttribute("manager", staff);
            model.addAttribute("name", staff.getName());
        }
        return "home/home";
    }
}
