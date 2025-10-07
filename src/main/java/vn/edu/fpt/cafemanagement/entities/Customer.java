package vn.edu.fpt.cafemanagement.entities;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cus_id", nullable = false)
    private Integer cusId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "point")
    private Integer point;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "username", nullable = false, length = 100, unique = true)
    private String username; // nvarchar(100), có thể cần thêm ràng buộc unique

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth; // date, có thể null

    @Column(name = "img", length = 255)
    private String img; // nvarchar(255), có thể null (thường lưu đường dẫn ảnh)

    @Column(name = "failed_attempts", nullable = false)
    private Integer failedAttempts = 0; // int, not null (Đặt giá trị mặc định là 0 là hợp lý)

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil; // datetime, có thể null (sử dụng LocalDateTime)

    public Customer() {
    }

    public Customer(Integer cusId, String name, String phoneNumber, String email, Integer point, String address, String password, String username, LocalDate dateOfBirth, String img, Integer failedAttempts, LocalDateTime lockedUntil) {
        this.cusId = cusId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.point = point;
        this.address = address;
        this.password = password;
        this.username = username;
        this.dateOfBirth = dateOfBirth;
        this.img = img;
        this.failedAttempts = failedAttempts;
        this.lockedUntil = lockedUntil;
    }

    public Integer getCusId() {
        return cusId;
    }

    public void setCusId(Integer cusId) {
        this.cusId = cusId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getPoint() {
        return point;
    }

    public void setPoint(Integer point) {
        this.point = point;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public Integer getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(Integer failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(LocalDateTime lockedUntil) {
        this.lockedUntil = lockedUntil;
    }
}
