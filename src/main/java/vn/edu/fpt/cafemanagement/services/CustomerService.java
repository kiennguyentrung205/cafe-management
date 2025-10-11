package vn.edu.fpt.cafemanagement.services;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.fpt.cafemanagement.entities.Customer;
import vn.edu.fpt.cafemanagement.entities.PointHistory;
import vn.edu.fpt.cafemanagement.repositories.CustomerRepository;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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

    public void updateCustomer (Customer customer, MultipartFile imgFile) {
        Customer existingCustomer = customerRepository.findById(customer.getCusId()).orElse(null);
        if (existingCustomer != null) {
            if(!existingCustomer.getEmail().equalsIgnoreCase(customer.getEmail())) {
                if (customerRepository.existsByEmailIgnoreCase(customer.getEmail())) {
                    throw new IllegalArgumentException("Email đã tồn tại, vui lòng dùng email khác!");
                }
                existingCustomer.setEmail(customer.getEmail());
            }
            if(!existingCustomer.getPhoneNumber().equalsIgnoreCase(customer.getPhoneNumber())) {
                if (customerRepository.existsByPhoneNumber(customer.getPhoneNumber())) {
                    throw new IllegalArgumentException("So dien thoai đã tồn tại, vui lòng dùng so dien thoai khác!");
                }
                existingCustomer.setPhoneNumber(customer.getPhoneNumber());
            }

            existingCustomer.setName(customer.getName());

            if (imgFile != null && !imgFile.isEmpty()) {
                try {
                    String uploadDir = "D:/FA25/HSF/img/";
                    String fileName = imgFile.getOriginalFilename();

                    // Lưu file vào thư mục uploads
                    Path filePath = Paths.get(uploadDir + fileName);
                    Files.copy(imgFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                    // Cập nhật tên ảnh trong DB
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
}

