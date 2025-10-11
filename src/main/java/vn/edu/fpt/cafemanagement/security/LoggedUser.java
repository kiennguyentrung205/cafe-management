package vn.edu.fpt.cafemanagement.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import vn.edu.fpt.cafemanagement.entities.CustomUserDetails;
import vn.edu.fpt.cafemanagement.entities.Customer;
import vn.edu.fpt.cafemanagement.entities.Manager;

@Component
public class LoggedUser {
    public CustomUserDetails getLoggedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return null;
        }
        return (CustomUserDetails) authentication.getPrincipal();
    }

    public Customer getLoggedCustomer() {
        CustomUserDetails userDetails = getLoggedUser();
        if (userDetails != null && userDetails.isCustomer()) {
            return userDetails.getCustomer();
        }
        return null;
    }

    public Manager getLoggedManager() {
        CustomUserDetails userDetails = getLoggedUser();

        if (userDetails != null && userDetails.isManager()) {
            return userDetails.getManager();
        }
        return null;
    }

    public boolean isCustomer() {
        CustomUserDetails userDetails = getLoggedUser();
        return userDetails != null && userDetails.isCustomer();
    }

    public boolean isManager() {
        CustomUserDetails userDetails = getLoggedUser();
        return userDetails != null && userDetails.isManager();
    }
}
