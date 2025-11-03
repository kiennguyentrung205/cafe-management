package vn.edu.fpt.cafemanagement.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import vn.edu.fpt.cafemanagement.entities.CustomUserDetails;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomLoginSuccessHandler.class);

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        logger.info("=== LOGIN SUCCESS ===");
        logger.info("Username: {}", authentication.getName());
        logger.info("Request URI: {}", request.getRequestURI());

        if(authentication.getPrincipal() instanceof OidcUser){
            response.sendRedirect("/home");
            return;
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        logger.info("User Type: {}", userDetails.getUserType());
        logger.info("Full Name: {}", userDetails.getFullName());
        logger.info("Authorities: {}", authentication.getAuthorities());

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String redirectUrl = "/home"; // default

        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();
            logger.info("Processing role: {}", role);

            if (role.equals("ROLE_ADMIN")) {
                redirectUrl = "/dashboard";
                break;
            } else if (role.equals("ROLE_CASHIER")) {
                redirectUrl = "/table/booking/management";
                break;
            } else if (role.equals("ROLE_CUSTOMER")) {
                redirectUrl = "/home";
                break;
            } else if (role.equals("ROLE_BARISTA")) {
                redirectUrl = "/order/edit";
                break;
            } else if (role.equals("ROLE_WAITER")) {
                redirectUrl = "/home";
                break;
            }
        }

        logger.info("Redirecting to: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }
}