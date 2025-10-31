package vn.edu.fpt.cafemanagement.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.cafemanagement.entities.Order;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    List<Order> findByCustomerCusId(int cusId);

    // Chỉ lấy đơn đang hoạt động (Pending, Paid)
    @Query("SELECT o FROM Order o WHERE LOWER(o.status) IN ('Pending', 'Paid')")
    Page<Order> findActiveOrders(Pageable pageable);

    // Lấy đơn lịch sử (Served, Canceled)
    @Query("SELECT o FROM Order o WHERE LOWER(o.status) IN ('Served', 'Canceled')")
    Page<Order> findHistoryOrders(Pageable pageable);

    Page<Order> findByStatusIn(List<String> statuses, Pageable pageable);

    Page<Order> findByStatusNotIn(List<String> statuses, Pageable pageable);

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0), COUNT(o), COUNT(o.voucher) " +
            "FROM Order o " +
            "WHERE o.createdAt >= :startDate AND o.createdAt < :endDate")
    List<Object[]> getSalesSummaryObject(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    List<Order> findAllByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime startDate, LocalDateTime endDate);
}
