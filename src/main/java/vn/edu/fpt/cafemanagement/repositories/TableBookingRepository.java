package vn.edu.fpt.cafemanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.cafemanagement.entities.TableBooking;

import java.util.List;

@Repository
public interface TableBookingRepository extends JpaRepository<TableBooking, Integer> {

    List<TableBooking> findByCustomer_CusId(int customerId);
//
//
//    List<TableBooking> findAlList();
}
