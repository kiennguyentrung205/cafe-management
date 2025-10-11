package vn.edu.fpt.cafemanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.cafemanagement.entities.Customer;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer>
{
    Optional<Customer> findByUsername(String name);
    Optional<Customer> findByEmail(String email);

    Customer findByPhoneNumber(String phoneNumber);
}
