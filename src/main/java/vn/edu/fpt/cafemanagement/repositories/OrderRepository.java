package vn.edu.fpt.cafemanagement.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.cafemanagement.entities.Order;

import java.util.List;

@Repository
public interface  OrderRepository extends JpaRepository<Order, Integer> {

    List<Order> findByCustomerCusId(int cusId);

    @Query("SELECT o FROM Order o WHERE o.isActive = false ORDER BY o.createdAt DESC")
    Page<Order> findActiveOrders(Pageable pageable);
}
