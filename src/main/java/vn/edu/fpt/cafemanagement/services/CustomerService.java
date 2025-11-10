package vn.edu.fpt.cafemanagement.services;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import vn.edu.fpt.cafemanagement.entities.Customer;
import vn.edu.fpt.cafemanagement.repositories.CustomerRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.fpt.cafemanagement.entities.PointHistory;
import vn.edu.fpt.cafemanagement.repositories.PointHistoryRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


@Service
public class CustomerService {
    CustomerRepository customerRepository;
    PointHistoryRepository pointHistoryRepository;
    StaffService staffService;

    public CustomerService(CustomerRepository customerRepository, StaffService staffService, PointHistoryRepository pointHistoryRepository) {
        this.customerRepository = customerRepository;
        this.staffService = staffService;
        this.pointHistoryRepository = pointHistoryRepository;
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

    public Customer save(Customer customer) {
        return customerRepository.save(customer);
    }


    public Customer getCustomerById(int cusId) {
        return customerRepository.getCustomerByCusId(cusId);
    }

    public List<PointHistory> getPointHistoryByCustomerId(int cusId) {
        return customerRepository.getPointHistoryByCustomerId(cusId);
    }

    public void updateCustomer(Customer customer, MultipartFile imgFile) {
        Customer existingCustomer = customerRepository.findById(customer.getCusId()).orElseThrow(() -> new IllegalArgumentException("Customer not found!"));
        if (!existingCustomer.isGoogleAccount()) {
            if (!existingCustomer.getEmail().equalsIgnoreCase(customer.getEmail())) {
                if (customerRepository.existsByEmailIgnoreCase(customer.getEmail())) {
                    throw new IllegalArgumentException("The email already exists, please use a different one!");
                }
                existingCustomer.setEmail(customer.getEmail());
            }
            if (imgFile != null && !imgFile.isEmpty()) {
                try {
                    String uploadDir = "D:/SWP/Project/uploads/";
                    String fileName = imgFile.getOriginalFilename();

                    Path filePath = Paths.get(uploadDir + fileName);
                    Files.copy(imgFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                    existingCustomer.setImg(fileName);
                } catch (IOException e) {
                    throw new IllegalArgumentException("Error uploading image!");
                }
            }
        }

        if (!customer.getUsername().equalsIgnoreCase(existingCustomer.getUsername())) {
            if (customer.getUsername() == null || customer.getUsername().isEmpty()) {
                throw new IllegalArgumentException("The phone number cannot be empty.");
            }
            if (!customer.getUsername().matches("^[a-z_][a-z0-9_]{0,14}$")) {
                throw new IllegalArgumentException("The username must be 2-15 characters long and contain only lowercase letters, digits, and underscores (_).");
            }
            if (customerRepository.existsByUsername(customer.getUsername())) {
                throw new IllegalArgumentException("The username already exists, please use a different one!");
            }
            existingCustomer.setUsername(customer.getUsername());
        }

        if (!customer.getPhoneNumber().equalsIgnoreCase(existingCustomer.getPhoneNumber())) {
            if (customer.getPhoneNumber() == null || customer.getPhoneNumber().isEmpty()) {
                throw new IllegalArgumentException("The phone number cannot be empty.");
            }
            if (customer.getPhoneNumber().length() != 10) {
                throw new IllegalArgumentException("The phone number must be 10 digits!");
            }
            if (!customer.getPhoneNumber().matches("\\d{10}")) {
                throw new IllegalArgumentException("The phone number must contain only digits!");
            }
            if (!customer.getPhoneNumber().matches("^(0)(?!\\1{9})\\d{9}$")) {
                throw new IllegalArgumentException("Invalid phone number! It must start with 0, contain exactly 10 digits, and not be all repeated digits.");
            }
            if (customerRepository.existsByPhoneNumber(customer.getPhoneNumber())) {
                throw new IllegalArgumentException("The phone number already exists, please use a different one!");
            }
            existingCustomer.setPhoneNumber(customer.getPhoneNumber());
        }

        if (customer.getName() == null || customer.getName().isEmpty()) {
            throw new IllegalArgumentException("The name cannot be empty.");
        }
        if (!customer.getName().matches("^[A-Za-zÀ-ỹ\\s]+$")) {
            throw new IllegalArgumentException("The name can only contain letters and spaces!");
        }
        existingCustomer.setName(customer.getName());
        if (customer.getDateOfBirth() == null) {
            throw new IllegalArgumentException("Birthdate cannot be null!");
        }
        if (existingCustomer.isGoogleAccount()) {
            LocalDate birthDate = customer.getDateOfBirth();
            int age = Period.between(birthDate, LocalDate.now()).getYears();
            if (age < 10 || age > 100) {
                throw new IllegalArgumentException("Customer age must be between 10 and 100 years old!");
            }
            existingCustomer.setDateOfBirth(customer.getDateOfBirth());
        }
        customerRepository.save(existingCustomer);

    }

    public void changePassword(int cusId, String newPassword, String confirmPassword, String currentPassword) {
        Customer customer = customerRepository.findById(cusId).orElseThrow(() -> new IllegalArgumentException("Customer not found!"));
        if (currentPassword == null || currentPassword.isEmpty()) {
            throw new IllegalArgumentException("The new password cannot be empty.");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            throw new IllegalArgumentException("The new password cannot be empty.");
        }
        if (confirmPassword == null || confirmPassword.isEmpty()) {
            throw new IllegalArgumentException("The confirm password cannot be empty.");
        }
        if (BCrypt.checkpw(currentPassword, customer.getPassword())) {
            if (newPassword.equals(confirmPassword)) {
                String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
                if (!newPassword.matches(passwordPattern)) {
                    throw new IllegalArgumentException("Password must be at least 8 characters long and include uppercase letters, " +
                            "lowercase letters, numbers, and special characters!\n");
                }
                String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
                customer.setPassword(hashedPassword);
                customerRepository.save(customer);
            } else {
                throw new IllegalArgumentException("New password does not match!");
            }
        } else {
            throw new IllegalArgumentException("Incorrect password!");
        }
    }

    public Customer getCustomerByPhone(String phoneNumber) {
        return customerRepository.findByPhoneNumber(phoneNumber);
    }

    public void saveCustomer(Customer customer) {
        customerRepository.save(customer);
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }


    @Transactional
    public void rewardBirthdayPoints() {
        List<Customer> customers = customerRepository.findAll();
        int currentYear = LocalDate.now().getYear();
        LocalDate today = LocalDate.now();
        int birthdayBonus = 50; // Số điểm tặng

        for (Customer customer : customers) {
            if (customer.getDateOfBirth() == null) continue;

            // Kiểm tra đúng ngày sinh và chưa được tặng trong năm nay
            if (customer.getDateOfBirth().getMonth() == today.getMonth() &&
                    customer.getDateOfBirth().getDayOfMonth() == today.getDayOfMonth() &&
                    (customer.getLastBirthdayRewardYear() == null || customer.getLastBirthdayRewardYear() < currentYear)) {

                // Cộng điểm
                customer.setPoint(customer.getPoint() + birthdayBonus);
                customer.setLastBirthdayRewardYear(currentYear);
                customerRepository.save(customer);

                //  Lưu lịch sử điểm
                PointHistory history = new PointHistory();
                history.setCustomer(customer);
                history.setTypeOfChange("Birthday Bonus");
                history.setAmount(birthdayBonus);
                history.setChangeTime(LocalDateTime.now());
                new java.util.Date();
                pointHistoryRepository.save(history);
            }
        }
    }
}

