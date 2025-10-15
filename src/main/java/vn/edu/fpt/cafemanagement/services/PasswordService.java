package vn.edu.fpt.cafemanagement.services;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import vn.edu.fpt.cafemanagement.entities.Customer;
import vn.edu.fpt.cafemanagement.repositories.CustomerRepository;

@Service
public class PasswordService {
    CustomerRepository customerRepository;
    OtpService otpService;
    JavaMailSender mailSender;

    public PasswordService(CustomerRepository customerRepository, OtpService otpService,JavaMailSender mailSender) {
        this.customerRepository = customerRepository;
        this.otpService = otpService;
        this.mailSender = mailSender;
    }

    // Gửi mã OTP qua email
    public void sendOtpToEmail(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email không tồn tại!"));

        String otp = otpService.generateOtp(email);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Mã đặt lại mật khẩu (hiệu lực 15 phút)");
        message.setText("Mã OTP của bạn là: " + otp + "\n\nMã này sẽ hết hạn sau 15 phút.");

        mailSender.send(message);
    }

    public void resetPassword(String email, String otp, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Mật khẩu không khớp!");
        }

        String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        if (!newPassword.matches(passwordPattern)) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt!");
        }

        if (!otpService.validateOtp(email, otp)) {
            throw new IllegalArgumentException("Mã OTP không đúng hoặc đã hết hạn!");
        }

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        customer.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        customerRepository.save(customer);
    }
}
