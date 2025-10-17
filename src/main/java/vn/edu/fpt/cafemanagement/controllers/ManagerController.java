package vn.edu.fpt.cafemanagement.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.fpt.cafemanagement.entities.Manager;
import vn.edu.fpt.cafemanagement.entities.Role;
import vn.edu.fpt.cafemanagement.services.ManagerService;
import vn.edu.fpt.cafemanagement.services.RoleService;

import java.util.List;

@Controller
@RequestMapping("/dashboard/staff")
public class ManagerController {

    private final ManagerService managerService;
    private final RoleService roleService;

    public ManagerController(ManagerService managerService, RoleService roleService) {
        this.managerService = managerService;
        this.roleService = roleService;
    }

    @RequestMapping
    public String list(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        List<Manager> list;
        if (keyword != null && !keyword.trim().isEmpty()) {
            list = managerService.searchStaff(keyword.trim());
        } else {
            list = managerService.getList();
        }
        model.addAttribute("staffs", list);
        model.addAttribute("keyword", keyword);

        if (list.isEmpty() && keyword != null && !keyword.trim().isEmpty()) {
            model.addAttribute("notFound", "No staff found for \"" + keyword + "\"");
        }
        return "dashboard/staff/list";
    }

    @GetMapping("/new")
    public String create(Model model) {
        model.addAttribute("staff", new Manager());
        model.addAttribute("roles", roleService.getAllRoles());
        return "dashboard/staff/create";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") int id, Model model) {
        Manager s = managerService.findById(id);
        model.addAttribute("staff", s);
        model.addAttribute("roles", roleService.getAllRoles());
        return "dashboard/staff/edit";
    }

        @PostMapping("/save")
        public String save(@ModelAttribute("staff") Manager staff,
                           @RequestParam("roleId") int roleId,
                           @RequestParam("photo") MultipartFile file,  Model model) {
            Role role = roleService.getRoleById(roleId)
                    .orElseThrow(() -> new IllegalArgumentException("Null"));
            staff.setRole(role);

            if (staff.getManagerId() == 0) {
                staff.setActive(true);
            }

            Integer idForCheck = (staff.getManagerId() == 0) ? null : staff.getManagerId();
            // check username/email/phone (service sẽ xử lý loại trừ bản thân nếu idForCheck != null)
            if (managerService.isUsernameTaken(staff.getUsername(), idForCheck)) {
                model.addAttribute("error", "Username already exists!");
                model.addAttribute("roles", roleService.getAllRoles());
                return (idForCheck == null) ? "dashboard/staff/create" : "dashboard/staff/edit";
            }

            if (managerService.isEmailTaken(staff.getEmail(), idForCheck)) {
                model.addAttribute("error", "Email already exists!");
                model.addAttribute("roles", roleService.getAllRoles());
                return (idForCheck == null) ? "dashboard/staff/create" : "dashboard/staff/edit";
            }

            if (managerService.isPhoneTaken(staff.getPhoneNumber(), idForCheck)) {
                model.addAttribute("error", "Phone number already exists!");
                model.addAttribute("roles", roleService.getAllRoles());
                return (idForCheck == null) ? "dashboard/staff/create" : "dashboard/staff/edit";
            }


            if (!file.isEmpty()) {
                try {
                    String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                    String uploadDir = System.getProperty("user.dir") + "/src/main/resources/static/img/staff/";//
                    java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);
                    if (!java.nio.file.Files.exists(uploadPath)) {
                        java.nio.file.Files.createDirectories(uploadPath);
                    }

                    file.transferTo(new java.io.File(uploadDir + fileName));
                    staff.setImg("/img/staff/" + fileName);

                } catch (Exception e) {
                    e.printStackTrace();
                }

        }
            managerService.save(staff);
            return "redirect:/dashboard/staff";
        }

    @GetMapping("/{id}")
    public String getStaffDetails(@PathVariable("id") int id, Model model) {
        Manager s = managerService.findById(id);
        List<Role> roles = roleService.getAllRoles();
        model.addAttribute("staff", s);
        model.addAttribute("roles", roles);
        return "dashboard/staff/detail";
    }

    // --- Soft delete ---
    @PostMapping("/delete/{id}")
    public String softDelete(@PathVariable int id) {
        managerService.softDelete(id); // chỉ set isActive = false
        return "redirect:/dashboard/staff";
    }

    // --- Danh sách nhân viên đã xóa ---
    @GetMapping("/deleted")
    public String deletedList(Model model) {
        model.addAttribute("staffs", managerService.getDeletedStaffs());
        return "dashboard/staff/deleted-staff"; // tạo file deleted-staff.html
    }

    // --- Restore nhân viên ---
    @PostMapping("/restore/{id}")
    public String restore(@PathVariable int id) {
        managerService.restore(id); // set isActive = true
        return "redirect:/dashboard/staff/deleted";
    }


}
