package vn.edu.fpt.cafemanagement.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.cafemanagement.entities.Banner;
import vn.edu.fpt.cafemanagement.repositories.BannerRepository;
import vn.edu.fpt.cafemanagement.services.BannerService;

import java.util.Optional;

@Controller
@RequestMapping("/dashboard/banners")
public class BannerController {
    private final BannerService bannerService;

    public BannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    @GetMapping
    public String listBanner(Model model) {
        model.addAttribute("banners", bannerService.findAllBanners());
        return "dashboard/banners/list";
    }

    @GetMapping("/create")
    public String createBanner(Model model) {
        model.addAttribute("banner", new Banner());
        model.addAttribute("pageTitle", "Add Banner");
        return "dashboard/banners/create";
    }

    @GetMapping("/edit/{id}")
    public String editBanner(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Banner> banner = bannerService.findBannerById(id);
        if (banner.isPresent()) {
            model.addAttribute("banner", banner.get());
            model.addAttribute("pageTitle", "Edit Banner (ID: " + id + ")");
            return "dashboard/banners/edit";
        } else {
            redirectAttributes.addFlashAttribute("errorInfo", "Banner ID " + id + " not found.");
            return "redirect:/dashboard/banners";
        }
    }

    @PostMapping("/save")
    public String saveBanner(@ModelAttribute Banner banner,
                             @RequestParam("imageFile") MultipartFile imageFile,
                             RedirectAttributes redirectAttributes) {
        try {
            if (!imageFile.isEmpty()) {
                // Tạo thư mục nếu chưa tồn tại
                String uploadDir = "D:/SWP/Project/uploads/";
                java.io.File dir = new java.io.File(uploadDir);
                if (!dir.exists()) dir.mkdirs();

                // Lưu file
                String fileName = imageFile.getOriginalFilename();
                java.nio.file.Path path = java.nio.file.Paths.get(uploadDir + fileName);
                imageFile.transferTo(path.toFile());

                // Lưu tên file vào DB
                banner.setImagePath("/uploads/" + fileName);
            }

            bannerService.save(banner);
            redirectAttributes.addFlashAttribute("completeInfo", "Banner has been saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorInfo", "Error saving banner: " + e.getMessage());
        }

        return "redirect:/dashboard/banners";
    }


    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        // 1. Thực hiện logic xóa
        bannerService.delete(id);

        // 2. (Tùy chọn) Thêm thông báo thành công
        redirectAttributes.addFlashAttribute("completeInfo", "Banner: " + id + " đã được xóa thành công.");

        // 3. Chuyển hướng trở lại trang danh sách (Banner Manager)
        return "redirect:/dashboard/banners"; // <--- ĐÃ SỬA THÀNH ĐƯỜNG DẪN HIỂN THỊ DANH SÁCH
    }

}



