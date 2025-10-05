package vn.edu.fpt.cafemanagement.repositories;

import org.springframework.data.repository.CrudRepository;
import vn.edu.fpt.cafemanagement.entities.Voucher;

public interface VoucherRepository extends CrudRepository<Voucher, Integer> {
}
