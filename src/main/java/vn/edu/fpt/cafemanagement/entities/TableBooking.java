package vn.edu.fpt.cafemanagement.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class TableBooking {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY) // Giả định là tự tăng (IDENTITY)
    @Column(name = "booking_id", nullable = false)
    private Integer bookingId;

    // --- Khóa ngoại (Foreign Keys) ---

//    // Quan hệ Many-to-One với Table
//    @ManyToOne
//    @JoinColumn(name = "table_id", nullable = false)
//    private TableEntity table; // Giả định Entity cho bảng 'Table' là TableEntity

    // Quan hệ Many-to-One với Customer
    @ManyToOne
    @JoinColumn(name = "cus_id", nullable = false)
    private Customer customer;

    // --- Các trường dữ liệu khác ---

    @Column(name = "status", length = 20)
    private String status; // varchar(20), có thể null

    @Column(name = "booking_time")
    private LocalDateTime bookingTime; // datetime, có thể null (sử dụng LocalDateTime)

    // --- Constructors, Getters, và Setters ---

    // Constructor mặc định (cần thiết cho JPA)
    public TableBooking() {
    }

    public Integer getBookingId() {
        return bookingId;
    }

    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(LocalDateTime bookingTime) {
        this.bookingTime = bookingTime;
    }
}
