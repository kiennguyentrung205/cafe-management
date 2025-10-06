package configs;

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
    // Method này dc Spring quản lí như 1 bean trong IoC
    // Nghĩa là filterChain sẽ được Spring tạo ra và dùng để cấu hình bảo mật.
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()).authorizeHttpRequests(auth -> auth.requestMatchers("/", "/login**", "/home").permitAll() // Cho phép mọi người (không cần login) truy cập vào / và /login**
                                .anyRequest().authenticated()
                        // Tất cả các request khác (không được liệt kê ở trên) bắt buộc phải đăng nhập mới truy cập được.
                )
                // Login truyền thống (username + password)
                .formLogin(form -> form.loginPage("/login") // GET /login -> trang login custom
//                        .loginProcessingUrl("/login")  // POST /login -> Spring tự xử lý
                        .defaultSuccessUrl("/artist", true) // Sau khi login thành công
                        .permitAll())
                // Login bằng Google (OAuth2)
                .oauth2Login(oauth2 -> oauth2.loginPage("/login")              // Dùng chung trang login
                        .defaultSuccessUrl("/artist", true) // Sau khi login bằng GG
                ).logout(logout -> logout.logoutSuccessUrl("/home").permitAll() // Ai cũng có thể gọi API logout.
                );

        return http.build(); // Xây dựng và trả về SecurityFilterChain đã cấu hình xong.
    }


}
