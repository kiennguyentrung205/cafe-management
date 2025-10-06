package vn.edu.fpt.cafemanagement.services;

import org.springframework.stereotype.Service;
import vn.edu.fpt.cafemanagement.entities.Voucher;
import vn.edu.fpt.cafemanagement.repositories.VoucherRepository;

import java.util.List;

@Service
public class VoucherService {
    private VoucherRepository voucherRepository;
    public VoucherService(VoucherRepository voucherRepository) {
        this.voucherRepository = voucherRepository;
    }
    public List<Voucher> findAllByVoucherId(String voucherId) {
        return voucherRepository.findAllByVoucherId(voucherId);
    }
}
