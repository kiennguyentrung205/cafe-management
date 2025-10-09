package vn.edu.fpt.cafemanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.cafemanagement.entities.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Customer findByPhoneNumber(String phoneNumber);
}
