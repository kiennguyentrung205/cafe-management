package vn.edu.fpt.cafemanagement.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.cafemanagement.entities.Voucher;
import vn.edu.fpt.cafemanagement.repositories.VoucherRepository;

import java.util.List;

@Service
public class VoucherService {
    private VoucherRepository voucherRepository;
    public VoucherService(VoucherRepository voucherRepository) {
        this.voucherRepository = voucherRepository;
    }
    public List<Voucher> findAll() {
        return voucherRepository.findAll();
    }
    @Transactional
    public Voucher save(Voucher voucher) {
        return voucherRepository.save(voucher);
    }
    public void delete(Voucher voucher) {
        voucherRepository.delete(voucher);
    }
    @Transactional
    public void deleteVoucherById(int id) {
        voucherRepository.deleteByVoucherId(id);
    }
    public Voucher findById(int voucherId) {
        return voucherRepository.findByVoucherId(voucherId);
    }

}
