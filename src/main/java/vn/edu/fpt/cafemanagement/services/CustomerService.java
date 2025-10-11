package vn.edu.fpt.cafemanagement.services;

import org.springframework.stereotype.Service;
import vn.edu.fpt.cafemanagement.entities.Customer;
import vn.edu.fpt.cafemanagement.repositories.CustomerRepository;
import vn.edu.fpt.cafemanagement.repositories.ManagerRepository;

@Service
public class CustomerService {
    ManagerService managerService;
    CustomerRepository customerRepository;
    public CustomerService(CustomerRepository customerRepository, ManagerService managerService) {
        this.customerRepository = customerRepository;
        this.managerService = managerService;
    }

    public Customer findByEmail(String email) {
        return customerRepository.findByEmail(email).orElse(null);
    }

    public Customer findByPhoneNumber(String phone) {
        return customerRepository.findByPhoneNumber(phone);
    }

    public Customer findByUsername(String username) {
        return customerRepository.findByUsername(username).orElse(null);
    }

    public Customer createCustomer(Customer customer) throws Exception {
        String username = customer.getUsername();
        String phoneNumber = customer.getPhoneNumber();

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or blank");
        }
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be null or blank");
        }

        boolean usernameExists =
                managerService.findByUsername(username) != null ||
                        findByUsername(username) != null;

        if (usernameExists) {
            throw new IllegalStateException("Username is already in use");
        }

        boolean phoneExists = findByPhoneNumber(phoneNumber) != null;

        if (phoneExists) {
            throw new IllegalStateException("Phone number is already in use");
        }

        return customerRepository.save(customer);
    }
}
