package vn.edu.fpt.cafemanagement.entities;

import jakarta.persistence.*;


@Entity
public class ShiftAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
