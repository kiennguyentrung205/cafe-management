package vn.edu.fpt.cafemanagement.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    // Nghĩa là filterChain sẽ được Spring tạo ra và dùng để cấu hình bảo mật.
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.requestMatchers("/", "/login**")
                        .permitAll()
                        .anyRequest()
                        .authenticated()

                )

                .formLogin(form -> form.loginPage("/login")
                        .loginProcessingUrl("/login")  // POST /login -> Spring tự xử lý
                        .defaultSuccessUrl("/home", true)
                        .permitAll())

                .oauth2Login(oauth2 -> oauth2.loginPage("/login")
                        .defaultSuccessUrl("/home", true)
                ).logout(logout -> logout.logoutSuccessUrl("/home").permitAll()
                );

        return http.build();
    }


}
