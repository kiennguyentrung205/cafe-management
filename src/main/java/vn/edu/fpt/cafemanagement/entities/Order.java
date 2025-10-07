package vn.edu.fpt.cafemanagement.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity

public class Order {

    @Id
    private int orderId;
    private int cusId;
    private int managerId;
    private String status;
    private double totalPrice;
    private LocalDateTime createdAt;
    private int pointUsed;
    private int voucherId;

    public Order() {
    }

    public Order(int orderId, int cusId, int managerId, String status, double totalPrice, LocalDateTime createdAt,
                 int pointUsed, int voucherId) {
        this.orderId = orderId;
        this.cusId = cusId;
        this.managerId = managerId;
        this.status = status;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt;
        this.pointUsed = pointUsed;
        this.voucherId = voucherId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getCusId() {
        return cusId;
    }

    public void setCusId(int cusId) {
        this.cusId = cusId;
    }

    public int getManagerId() {
        return managerId;
    }

    public void setManagerId(int managerId) {
        this.managerId = managerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getPointUsed() {
        return pointUsed;
    }

    public void setPointUsed(int pointUsed) {
        this.pointUsed = pointUsed;
    }

    public int getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(int voucherId) {
        this.voucherId = voucherId;
    }
}