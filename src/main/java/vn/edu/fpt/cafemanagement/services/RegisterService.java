package vn.edu.fpt.cafemanagement.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import vn.edu.fpt.cafemanagement.entities.Customer;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

@Controller
public class RegisterService {
    private CustomerService customerService;
    private ManagerService managerService;

    public RegisterService(CustomerService customerService, ManagerService managerService) {
        this.customerService = customerService;
        this.managerService = managerService;
    }

    public Customer createCustomer(Customer customer) throws Exception {
        String fullName = customer.getName();
        String username = customer.getUsername();
        String phoneNumber = customer.getPhoneNumber();
        String email = customer.getEmail();
        String password = customer.getPassword();
        LocalDate dob = customer.getDateOfBirth();


        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Full name cannot be null or blank");
        }
        if (!fullName.matches("^[\\p{L} ]+$")) {
            throw new IllegalArgumentException("Full name can only contain letters");
        }

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or blank");
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Username can only contain letters, digits, and underscores");
        }
        boolean usernameExists =
                managerService.findByUsername(username) != null ||
                        customerService.findByUsername(username) != null;
        if (usernameExists) {
            throw new IllegalStateException("Username is already in use");
        }


        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }
        if (!email.matches("^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        boolean emailExists = managerService.findByEmail(email) != null || customerService.findByEmail(email) != null;
        if (emailExists) {
            throw new Exception("Email already exists");
        }

        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be null or blank");
        }
        if (!phoneNumber.matches("\\d{10}")) {
            throw new IllegalArgumentException("Phone number must contain exactly 10 digits");
        }
        if (phoneNumber.equals("0000000000")) {
            throw new IllegalArgumentException("Phone number cannot be all zeros");
        }
        boolean phoneExists = customerService.findByPhoneNumber(phoneNumber) != null;
        if (phoneExists) {
            throw new IllegalStateException("Phone number is already in use");
        }


        if (dob == null) {
            throw new IllegalArgumentException("Date of birth cannot be null");
        }
        LocalDate today = LocalDate.now();
        int age = Period.between(dob, today).getYears();
        if (age < 15 || age > 100) {
            throw new IllegalArgumentException("Age must be between 15 and 100 years old");
        }


        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }
        if (password.length() < 8 || password.length() > 64) {
            throw new IllegalArgumentException("Password must be between 8 and 64 characters");
        }
        if (password.contains(" ")) {
            throw new IllegalArgumentException("Password must not contain spaces");
        }
        if (!password.matches("(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+")) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter, one lowercase letter, one digit and one special character");
        }

        return customerService.save(customer);
    }
}
