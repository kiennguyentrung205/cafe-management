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
}
