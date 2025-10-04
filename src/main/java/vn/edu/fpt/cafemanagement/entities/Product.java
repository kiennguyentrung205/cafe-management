package vn.edu.fpt.cafemanagement.entities;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pro_id")
    private int proId;

    @Column(name = "pro_name")
    private String proName;

    @Column(name = "img")
    private String img;

    @ManyToOne
    @JoinColumn(name = "cate_id", nullable = false)
    private Category category;

    @Column(name = "price")
    private double price;

    @Column(name = "status")
    private String status;

    @Column(name = "description")
    private String description;

    @Column(name = "is_active")
    private boolean isActive;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;
}
