package vn.edu.fpt.cafemanagement.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import vn.edu.fpt.cafemanagement.services.CustomOidcUserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ApplicationContext applicationContext) throws Exception {
        CustomOidcUserService oidcUserService = applicationContext.getBean(CustomOidcUserService.class);

        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers( "/login**", "/assets/**","/forgot-password", "/set-password**", "/register", "/home")
                        .permitAll()
                        .requestMatchers( "/table/booking/management").hasRole("CASHIER")
                        .requestMatchers( "/dashboard/admin/vouchers/list").hasRole("ADMIN")
                        .requestMatchers( "/dashboard/admin/vouchers/create").hasRole("ADMIN")
                        .requestMatchers( "/dashboard/admin/vouchers/edit/**").hasRole("ADMIN")
                        .requestMatchers( "/dashboard/admin/vouchers/deleted-list").hasRole("ADMIN")
                        .requestMatchers( "/product/list").hasRole("ADMIN")
                        .requestMatchers( "/dashboard/staff").hasRole("ADMIN")
                        .requestMatchers( "/dashboard").hasRole("ADMIN")
                        .anyRequest()
                        .authenticated()
                )

                .exceptionHandling(exc -> exc
                        .accessDeniedHandler(accessDeniedHandler())
                )

                .formLogin(form -> form.loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/home", true)
                        .permitAll())

                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(oidcUserService)
                        )
                        .defaultSuccessUrl("/home", true))

                .logout(logout ->logout
                        .logoutUrl("/logout")
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                        .logoutSuccessUrl("/login?logout=success")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            // Nếu là AJAX hoặc REST API (trả JSON)
            String accept = request.getHeader("Accept");
            String xrw = request.getHeader("X-Requested-With");

            if ("XMLHttpRequest".equalsIgnoreCase(xrw) ||
                    (accept != null && accept.contains("application/json"))) {

                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\":\"Forbidden\",\"message\":\"Access denied\"}");
            } else {
                // Nếu là web (browser), redirect sang trang lỗi 403
                response.sendRedirect(request.getContextPath() + "/403");
            }
        };
    }



}
