package vn.edu.fpt.cafemanagement.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class PointHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pointhistory_id")
    private int pointHistoryId;

    @ManyToOne
    @JoinColumn(name = "cus_id", nullable = false)
    private Customer customer;

    @Column(name = "type_of_change")
    private String typeOfChange;

    @Column(name = "change_time")
    private LocalDateTime changeTime;

    @Column(name = "amount")
    private int amount;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "status")
    private String status;

    @Column(name = "booking_time")
    private LocalDateTime bookingTime;

    public PointHistory() {
    }

    public PointHistory(int pointHistoryId, Customer customer, String typeOfChange, LocalDateTime changeTime,
                        int amount, Order order, String status, LocalDateTime bookingTime) {
        this.pointHistoryId = pointHistoryId;
        this.customer = customer;
        this.typeOfChange = typeOfChange;
        this.changeTime = changeTime;
        this.amount = amount;
        this.order = order;
        this.status = status;
        this.bookingTime = bookingTime;
    }

    public int getPointHistoryId() {
        return pointHistoryId;
    }

    public void setPointHistoryId(int pointHistoryId) {
        this.pointHistoryId = pointHistoryId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getTypeOfChange() {
        return typeOfChange;
    }

    public void setTypeOfChange(String typeOfChange) {
        this.typeOfChange = typeOfChange;
    }

    public LocalDateTime getChangeTime() {
        return changeTime;
    }

    public void setChangeTime(LocalDateTime changeTime) {
        this.changeTime = changeTime;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
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
