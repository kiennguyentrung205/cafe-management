package vn.edu.fpt.cafemanagement.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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
    public String saveBanner(@ModelAttribute Banner banner, RedirectAttributes redirectAttributes) {
        try {
            // **LƯU Ý quan trọng về file upload: Bạn cần thêm logic xử lý upload file ảnh ở đây
            // (Ví dụ: lưu file vào thư mục và cập nhật imagePath)

            bannerService.save(banner);
            redirectAttributes.addFlashAttribute("completeInfo", "Banner has been saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorInfo", "Error saving banners: " + e.getMessage());
        }
        return "redirect:/dashboard/banners";
    }
}


