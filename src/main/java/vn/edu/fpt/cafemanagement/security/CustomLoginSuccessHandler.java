package vn.edu.fpt.cafemanagement.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String redirectUrl = "/home"; // default

        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();
            if (role.equals("ROLE_ADMIN")) {
                redirectUrl = "/dashboard";
                break;
            } else if (role.equals("ROLE_CASHIER")) {
                redirectUrl = "/home";
                break;
            } else if (role.equals("ROLE_CUSTOMER")) {
                redirectUrl = "/home";
                break;
            } else if (role.equals("ROLE_BARISTA")) {
                redirectUrl = "/order/edit";
                break;
            }
        }

        response.sendRedirect(redirectUrl);
    }
}