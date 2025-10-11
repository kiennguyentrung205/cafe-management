package vn.edu.fpt.cafemanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.cafemanagement.entities.Customer;
import vn.edu.fpt.cafemanagement.entities.PointHistory;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer>
{
    Optional<Customer> findByUsername(String name);

    Customer getCustomerByCusId(int cusId);

    @Query(
            value = "SELECT pointhistory_id, type_of_change, change_time, amount, cus_id, order_id " +
                    "FROM dbo.PointHistory " +
                    "WHERE cus_id = :cusId " +
                    "ORDER BY change_time DESC",
            nativeQuery = true)
    List<PointHistory> getPointHistoryByCustomerId(@Param("cusId") int cusId);
}
