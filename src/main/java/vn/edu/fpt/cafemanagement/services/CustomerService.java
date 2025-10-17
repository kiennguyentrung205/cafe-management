package vn.edu.fpt.cafemanagement.services;

import org.springframework.stereotype.Service;
import vn.edu.fpt.cafemanagement.entities.Customer;
import vn.edu.fpt.cafemanagement.repositories.CustomerRepository;
import java.util.List;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.fpt.cafemanagement.entities.PointHistory;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;


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


    public Customer getCustomerById(int cusId) {
        return customerRepository.getCustomerByCusId(cusId);
    }

    public List<PointHistory> getPointHistoryByCustomerId(int cusId) {
        return customerRepository.getPointHistoryByCustomerId(cusId);
    }

    public void updateCustomer(Customer customer, MultipartFile imgFile) {
        Customer existingCustomer = customerRepository.findById(customer.getCusId()).orElse(null);
        if (existingCustomer != null) {
            if (!existingCustomer.getEmail().equalsIgnoreCase(customer.getEmail())) {
                if (customerRepository.existsByEmailIgnoreCase(customer.getEmail())) {
                    throw new IllegalArgumentException("Email đã tồn tại, vui lòng dùng email khác!");
                }
                existingCustomer.setEmail(customer.getEmail());
            }

            if (!customer.getPhoneNumber().equalsIgnoreCase(existingCustomer.getPhoneNumber())) {
                if (customerRepository.existsByPhoneNumber(customer.getPhoneNumber())) {
                    throw new IllegalArgumentException("So dien thoai đã tồn tại, vui lòng dùng so dien thoai khác!");
                }
                existingCustomer.setPhoneNumber(customer.getPhoneNumber());
            }

            existingCustomer.setName(customer.getName());
            // Nhut Them Update Birthdate
            existingCustomer.setDateOfBirth(customer.getDateOfBirth());

            if (imgFile != null && !imgFile.isEmpty()) {
                try {
                    String uploadDir = "D:/SWP/Project/uploads/";
                    String fileName = imgFile.getOriginalFilename();

                    Path filePath = Paths.get(uploadDir + fileName);
                    Files.copy(imgFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                    existingCustomer.setImg(fileName);
                } catch (IOException e) {
                    throw new IllegalArgumentException("Lỗi khi tải ảnh lên!");
                }
            }

            customerRepository.save(existingCustomer);
        }
    }

    public void changePassword(int cusId, String newPassword, String confirmPassword, String currentPassword) {
        Customer customer = customerRepository.findById(cusId).orElse(null);
        if (customer != null) {
            if (BCrypt.checkpw(currentPassword, customer.getPassword())) {
                if (newPassword.equals(confirmPassword)) {
                    String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
                    if (!newPassword.matches(passwordPattern)) {
                        throw new IllegalArgumentException("Mật khẩu phải có ít nhất 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt!");
                    }
                    String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
                    customer.setPassword(hashedPassword);
                    customerRepository.save(customer);
                } else {
                    throw new IllegalArgumentException("Password moi khong khop");
                }
            } else {
                throw new IllegalArgumentException("Sai password");
            }
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
}

