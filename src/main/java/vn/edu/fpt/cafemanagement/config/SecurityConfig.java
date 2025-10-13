package vn.edu.fpt.cafemanagement.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.web.SecurityFilterChain;
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
                        .requestMatchers( "/login**", "/assets/**", "/register","/product/list")
                        .permitAll()
                        .anyRequest()
                        .authenticated()
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
                        .successHandler((req, res, auth) -> {
                            System.out.println("OAuth2 login success: " + auth.getPrincipal());
                            res.sendRedirect("/home");
                        })
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


}
