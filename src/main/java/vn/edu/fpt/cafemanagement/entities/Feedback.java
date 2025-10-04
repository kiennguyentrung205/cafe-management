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

}
