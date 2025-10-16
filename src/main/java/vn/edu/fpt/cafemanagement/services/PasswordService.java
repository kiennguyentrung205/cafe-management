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

    public void sendOtpToEmail(String email) {
        customerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email does not exist!"));

        String otp = otpService.generateOtp(email);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Password reset code (valid for 15 minutes)");
        message.setText("Your OTP code is: " + otp + "\n\nThis code will expire in 15 minutes.");

        mailSender.send(message);
    }

    public void resetPassword(String email, String otp, String newPassword, String confirmPassword) {
        if (newPassword == null || newPassword.isEmpty()) {
            throw new IllegalArgumentException("The new password cannot be empty.");
        }
        if (confirmPassword == null || confirmPassword.isEmpty()) {
            throw new IllegalArgumentException("The confirm password cannot be empty.");
        }
        if (otp == null || otp.isEmpty()) {
            throw new IllegalArgumentException("The otp cannot be empty.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Password does not match!");
        }

        String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        if (!newPassword.matches(passwordPattern)) {
            throw new IllegalArgumentException("Password must be at least 8 characters long and include uppercase letters, " +
                    "lowercase letters, numbers, and special characters!\n");
        }

        if (!otpService.validateOtp(email, otp)) {
            throw new IllegalArgumentException("The OTP code is incorrect or has expired!");
        }

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found!"));
        customer.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        customerRepository.save(customer);
    }
}
