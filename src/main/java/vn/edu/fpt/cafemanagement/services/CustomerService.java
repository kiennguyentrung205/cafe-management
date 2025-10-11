package vn.edu.fpt.cafemanagement.services;

import org.springframework.stereotype.Service;
import vn.edu.fpt.cafemanagement.entities.Customer;
import vn.edu.fpt.cafemanagement.entities.PointHistory;
import vn.edu.fpt.cafemanagement.repositories.CustomerRepository;

import java.util.List;

@Service
public class CustomerService {

    private CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer getCustomerById (int cusId) {
        return customerRepository.getCustomerByCusId(cusId);
    }

    public List<PointHistory> getPointHistoryByCustomerId (int cusId) {
        return customerRepository.getPointHistoryByCustomerId(cusId);
    }
}
