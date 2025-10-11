package vn.edu.fpt.cafemanagement.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
        List<Voucher> activeVouchers = voucherService.findAll()
                .stream()
                .filter(Voucher::isActive)
                .collect(Collectors.toList());
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
        return "/admin/vouchers/update";
    }

    @RequestMapping(value = "/admin/vouchers/save", method = RequestMethod.POST)
    public String save(@ModelAttribute(name = "voucher") Voucher voucher) {
        voucher.setActive(true);
        voucherService.save(voucher);
        return "redirect:/admin/vouchers/list";
    }

//    @RequestMapping(value = "/admin/vouchers/delete/{id}")
//    public String deleteVoucher(Model model, @PathVariable("id") int id) {
//        model.addAttribute("voucher", voucherService.findById(id));
//        return "/admin/vouchers/delete";
//    }

    @RequestMapping(value = "/admin/vouchers/delete", method = RequestMethod.POST)
    public String deleteVoucher(@ModelAttribute(name = "voucher") Voucher voucher, @RequestParam("voucherId") int id) {
        voucher = voucherService.findById(id);
        if(voucher != null) {
            voucher.setActive(false);
            voucherService.save(voucher);
        }
//        voucherService.deleteVoucherById(voucher.getVoucherId());
        return "redirect:/admin/vouchers/list";
    }

}
