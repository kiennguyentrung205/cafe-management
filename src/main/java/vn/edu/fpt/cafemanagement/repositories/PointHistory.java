package vn.edu.fpt.cafemanagement.repositories;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class PointHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pointhistory_id")
    private int pointHistoryId;

    @Column(name = "cus_id")
    private int cusId;  //

    @Column(name = "type_of_change", length = 100)
    private String typeOfChange;

    @Column(name = "change_time")
    private LocalDateTime changeTime;

    @Column(name = "amount", nullable = false)
    private int amount;

    @Column(name = "order_id")
    private Integer orderId; //

    public PointHistory() {}

    public PointHistory(int pointHistoryId, int cusId, String typeOfChange,
                        LocalDateTime changeTime, int amount, Integer orderId) {
        this.pointHistoryId = pointHistoryId;
        this.cusId = cusId;
        this.typeOfChange = typeOfChange;
        this.changeTime = changeTime;
        this.amount = amount;
        this.orderId = orderId;
    }

    public int getPointHistoryId() {
        return pointHistoryId;
    }

    public void setPointHistoryId(int pointHistoryId) {
        this.pointHistoryId = pointHistoryId;
    }

    public int getCusId() {
        return cusId;
    }

    public void setCusId(int cusId) {
        this.cusId = cusId;
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

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }
}