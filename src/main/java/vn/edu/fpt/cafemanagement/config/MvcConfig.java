package vn.edu.fpt.cafemanagement.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. Ánh xạ tất cả các request có tiền tố /uploads/**
        // 2. Tới thư mục tuyệt đối trên ổ đĩa là D:/SWP/Project/uploads/
        // LƯU Ý: Phải có dấu / ở cuối đường dẫn (Ví dụ: "file:///D:/SWP/Project/uploads/")

        String imagePath = "file:///D:/SWP/Project/uploads/";
        // Đảm bảo đường dẫn này kết thúc bằng dấu "/"

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(imagePath);
    }
}
