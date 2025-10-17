package vn.edu.fpt.cafemanagement.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.cafemanagement.entities.Voucher;
import vn.edu.fpt.cafemanagement.services.VoucherService;

import java.util.List;
import java.util.stream.Collectors;


@Controller
public class VoucherController {
    private final VoucherService voucherService;

    public VoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @GetMapping(value = {"/admin/vouchers", "/admin/vouchers/list"})
    public String getVouchers(Model model) {
        List<Voucher> activeVouchers = voucherService.findAll().stream().filter(Voucher::isActive).collect(Collectors.toList());
        model.addAttribute("vouchers", activeVouchers);
        return "/admin/vouchers/list";
    }

    @RequestMapping(value = "/admin/vouchers/create")
    public String createVoucher(Model model) {
        model.addAttribute("voucher", new Voucher());
        return "admin/vouchers/create";
    }

    @RequestMapping(value = "/admin/vouchers/edit/{id}")
    public String editVoucher(Model model, @PathVariable("id") int id) {
        model.addAttribute("voucher", voucherService.findById(id));
        return "admin/vouchers/edit";
    }

    @RequestMapping(value = "/admin/vouchers/save", method = RequestMethod.POST)
    public String save(@Validated @ModelAttribute(name = "voucher") Voucher voucher, BindingResult bindingResult, Model model) {
//        exception data
        if (bindingResult.hasErrors()) {
            model.addAttribute("message", "Error: Invalid! Please try again.");
            if (voucher.getVoucherId() == 0) {
                //  create
                return "admin/vouchers/create";
            } else {
                //  edit
                return "admin/vouchers/edit";
            }
        }
//        validate blank
        if (voucher.getCode().isBlank() || voucher.getVoucherName().isBlank() || voucher.getQuantity() == 0 || voucher.getDiscountType().isBlank() || voucher.getDiscountValue() == 0.0 || voucher.getMinOrderValue() < 0.0) {
            model.addAttribute("message", "Error: Please fill all the fields!");
            if (voucher.getVoucherId() == 0) {
                //  create
                return "admin/vouchers/create";
            } else {
                //  edit
                return "admin/vouchers/edit";
            }
        }
//        validate voucher name and voucher code
        if (!voucher.getCode().matches("[A-Za-z0-9%]+") || !voucher.getVoucherName().matches("[\\p{L}0-9%\\s]+")) {
            model.addAttribute("message", "Error: Voucher Name or Voucher Code is invalid!. EX: VCH123 or DISCOUNT50%");
            if (voucher.getVoucherId() == 0) {
                //  create
                return "admin/vouchers/create";
            } else {
                //  edit
                return "admin/vouchers/edit";
            }
        }
//       Validate Date
        if (voucher.getStartDate() == null || voucher.getEndDate() == null) {
            model.addAttribute("message", "Error: Please select both start and end dates!");
            return voucher.getVoucherId() == 0 ? "admin/vouchers/create" : "edit";
        }
        if (voucher.getStartDate().isAfter(voucher.getEndDate())) {
            model.addAttribute("message", "Error: Start Date cannot be after End Date!");
            if (voucher.getVoucherId() == 0) {
                //  create
                return "admin/vouchers/create";
            } else {
                //  edit
                return "admin/vouchers/edit";
            }
        }
        if (voucher.getEndDate().isBefore(voucher.getStartDate())) {
            model.addAttribute("message", "Error: End Date cannot be before Start Date!");
            if (voucher.getVoucherId() == 0) {
                //  create
                return "admin/vouchers/create";
            } else {
                //  edit
                return "admin/vouchers/edit";
            }
        }
//        check duplicate
        for (Voucher list : voucherService.findAll()) {
            if (voucher.getVoucherName().equals(list.getVoucherName()) || voucher.getCode().equals(list.getCode())) {
                model.addAttribute("message", "Error: Voucher Name or Code already exists!");
                if (voucher.getVoucherId() == 0) {
                    //  create
                    return "admin/vouchers/create";
                } else {
                    //  edit
                    return "admin/vouchers/edit";
                }
            }
        }
        voucher.setActive(true);
        voucherService.save(voucher);
        return "redirect:/admin/vouchers/list";
    }

    @RequestMapping(value = "/admin/vouchers/remove", method = RequestMethod.POST)
    public String remove(@ModelAttribute(name = "voucher") Voucher voucher, @RequestParam("voucherId") int id) {
        voucher = voucherService.findById(id);
        if (voucher != null) {
            voucher.setActive(false);
            voucherService.save(voucher);
        }

        return "redirect:/admin/vouchers/list";
    }

    @RequestMapping(value = "/admin/vouchers/deleted-list")
    public String trashVoucher(Model model) {
        model.addAttribute("vouchers", voucherService.getNoActiveVouchers());
        return "admin/vouchers/deleted-list";
    }

    @RequestMapping(value = "/admin/vouchers/restore")
    public String restore(Model model, Voucher voucher, @RequestParam(name = "voucherId") int id) {
        System.out.println("HELLLLLL");
        voucher = voucherService.findById(id);
        voucher.setActive(true);
        voucherService.save(voucher);
        model.addAttribute("vouchers", voucherService.getNoActiveVouchers());
        return "admin/vouchers/deleted-list";
    }
}
