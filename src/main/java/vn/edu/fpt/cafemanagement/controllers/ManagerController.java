package vn.edu.fpt.cafemanagement.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.cafemanagement.entities.Manager;
import vn.edu.fpt.cafemanagement.entities.Role;
import vn.edu.fpt.cafemanagement.repositories.ManagerRepository;
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
    public String list(Model model) {
        List<Manager> list = managerService.getList();
        model.addAttribute("staffs", list);
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


    @RequestMapping(value = "/delete/{id}")
    public String delete(@PathVariable("id") int id, Model model) {
        model.addAttribute("staff", managerService.findById(id));
        return "dashboard/staff/delete";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("staff") Manager staff,
                       @RequestParam("roleId") int roleId) {
        Role role = roleService.getRoleById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role không tồn tại!"));
        staff.setRole(role);
        managerService.save(staff);
        return "redirect:/dashboard/staff";
    }



    @PostMapping("/delete/{id}")
    public String doDelete(@PathVariable int id) {
        managerService.deleteById(id);
        return "redirect:/dashboard/staff";
    }

}
