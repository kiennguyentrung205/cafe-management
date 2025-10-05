package vn.edu.fpt.cafemanagement.entities;

import jakarta.persistence.*;


@Entity
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private int feedbackId;

    @ManyToOne
    @JoinColumn(name = "cus_id", nullable = false)
    private Customer customer;

    public Feedback() {
    }

    public Feedback(int feedbackId, Customer customer) {
        this.feedbackId = feedbackId;
        this.customer = customer;
    }

    public int getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(int feedbackId) {
        this.feedbackId = feedbackId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}
