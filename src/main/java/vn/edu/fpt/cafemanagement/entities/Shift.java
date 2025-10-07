package vn.edu.fpt.cafemanagement.entities;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Shift {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Giả định shift_id là IDENTITY (tự tăng) trong SQL Server
    @Column(name = "shift_id", nullable = false)
    private Integer shiftId;

    @Column(name = "shift_date", nullable = false)
    private LocalDate shiftDate; // Sử dụng LocalDate cho kiểu date

    @Column(name = "shift_period")
    private String shiftPeriod; // Kiểu varchar(20)

    // Khóa ngoại (Foreign Key) role_id.
    // Giả định bạn có một Entity tên là Role
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "role_id")
//    private Role role; // Sử dụng Role Entity để biểu diễn quan hệ

    @Column(name = "created_at")
    private LocalDateTime createdAt; // Sử dụng LocalDateTime cho kiểu datetime2(7)

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // Sử dụng LocalDateTime cho kiểu datetime2(7)

    // Constructors, Getters và Setters (Không bắt buộc phải có trong câu trả lời này, nhưng cần thiết trong thực tế)

    // Ví dụ về constructor mặc định
    public Shift() {
    }

    // Ví dụ về getters và setters

    public Integer getShiftId() {
        return shiftId;
    }

    public void setShiftId(Integer shiftId) {
        this.shiftId = shiftId;
    }

    public LocalDate getShiftDate() {
        return shiftDate;
    }

    public void setShiftDate(LocalDate shiftDate) {
        this.shiftDate = shiftDate;
    }

    public String getShiftPeriod() {
        return shiftPeriod;
    }

    public void setShiftPeriod(String shiftPeriod) {
        this.shiftPeriod = shiftPeriod;
    }
//
//    public Role getRole() {
//        return role;
//    }
//
//    public void setRole(Role role) {
//        this.role = role;
//    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Ghi chú: Cần phải tạo Entity Role.java nếu chưa có.
}


