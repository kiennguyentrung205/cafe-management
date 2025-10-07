package vn.edu.fpt.cafemanagement.entities;

import jakarta.persistence.*;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "Feedback")
public class Feedback {
    @Id
    @Column(name = "feedback_id")
    private int id;

    private Customer customer;

    private Product product;

    @Column(name = "content")
    private String content;

    @Column(name = "create_at")
    private LocalDateTime create_at;

    public Feedback() {
    }

    public Feedback(int id, Customer customer, Product product, String content, LocalDateTime create_at) {
        this.id = id;
        this.customer = customer;
        this.product = product;
        this.content = content;
        this.create_at = create_at;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreate_at() {
        return create_at;
    }

    public void setCreate_at(LocalDateTime create_at) {
        this.create_at = create_at;
    }
}
