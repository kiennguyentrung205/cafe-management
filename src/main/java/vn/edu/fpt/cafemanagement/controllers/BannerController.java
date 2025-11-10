package vn.edu.fpt.cafemanagement.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.cafemanagement.entities.Banner;
import vn.edu.fpt.cafemanagement.entities.Voucher;
import vn.edu.fpt.cafemanagement.repositories.BannerRepository;
import vn.edu.fpt.cafemanagement.services.BannerService;
import vn.edu.fpt.cafemanagement.util.SignUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/dashboard/banners")
public class BannerController {
    private final BannerService bannerService;

    public BannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    @GetMapping
    public String listBanner(
            Model model,
            @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable) {
        Page<Banner> bannerPage = bannerService.findAllBanners(pageable);
        model.addAttribute("bannerPage", bannerPage);

        List<Banner> activeBanners = bannerPage.getContent().stream()
                .filter(Banner::isActive)
                .collect(Collectors.toList());


        model.addAttribute("banners", bannerPage.getContent());
        model.addAttribute("activeBanners", activeBanners);

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
            String uploadDir = "D:/SWP/Project/uploads/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            if (!imageFile.isEmpty()) {
                // Lấy tên file gốc và phần mở rộng
                String originalFileName = imageFile.getOriginalFilename();
                String extension = "";
                if (originalFileName != null && originalFileName.contains(".")) {
                    extension = originalFileName.substring(originalFileName.lastIndexOf("."));
                }

                // Tạo tên file mới (UUID tránh trùng và bị khóa)
                String newFileName = java.util.UUID.randomUUID().toString() + extension;
                Path path = Paths.get(uploadDir + newFileName);

                // Ghi file mới
                Files.copy(imageFile.getInputStream(), path);

                // Cập nhật đường dẫn mới vào DB
                banner.setImagePath("/uploads/" + newFileName);
            }

            // Lưu DB
            bannerService.save(banner);
            redirectAttributes.addFlashAttribute("completeInfo", "Banner has been saved successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorInfo", "Error saving banner: " + e.getMessage());
        }

        return "redirect:/dashboard/banners";
    }





    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        // 1. Thực hiện logic xóa
        bannerService.delete(id);

        // 2. (Tùy chọn) Thêm thông báo thành công
        redirectAttributes.addFlashAttribute("completeInfo", "Banner: " + id + "vvv");

        // 3. Chuyển hướng trở lại trang danh sách (Banner Manager)
        return "redirect:/dashboard/banners"; // <--- ĐÃ SỬA THÀNH ĐƯỜNG DẪN HIỂN THỊ DANH SÁCH
    }

}



