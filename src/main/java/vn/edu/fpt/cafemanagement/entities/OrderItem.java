package vn.edu.fpt.cafemanagement.entities;

import jakarta.persistence.*;


@Entity
public class OrderItem {

    @Id
    private int orderItemId;
    private int orderId;
    private int productId;
    private int quantity;
    private String note;

    public OrderItem() {
    }

    public OrderItem(int orderItemId, int orderId, int productId, int quantity, String note) {
        this.orderItemId = orderItemId;
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.note = note;
    }

    public int getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(int orderItemId) {
        this.orderItemId = orderItemId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
