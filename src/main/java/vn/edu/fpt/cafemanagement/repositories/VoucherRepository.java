package vn.edu.fpt.cafemanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.cafemanagement.entities.Voucher;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Integer> {

    List<Voucher> findByIsActiveTrue();
    List<Voucher> findByIsActiveFalse();
    Voucher findByVoucherId(int voucherId);

    void deleteByVoucherId(int voucherId);

    /**
     * TRUY VẤN MỚI:
     * Chỉ lấy voucher:
     * 1. Đang active (is_active = true)
     * 2. Vẫn còn số lượng (quantity > 0)
     * 3. Vẫn còn hạn (endDate >= ngày hôm nay)
     */
    @Query("SELECT v FROM Voucher v WHERE v.isActive = true " +
            "AND v.quantity > 0 " +
            "AND (v.endDate >= :currentDate)")
    List<Voucher> findAvailableVouchers(@Param("currentDate") LocalDate currentDate);
}
