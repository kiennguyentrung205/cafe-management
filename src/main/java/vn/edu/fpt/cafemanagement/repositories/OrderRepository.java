package vn.edu.fpt.cafemanagement.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.cafemanagement.entities.Order;

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

}