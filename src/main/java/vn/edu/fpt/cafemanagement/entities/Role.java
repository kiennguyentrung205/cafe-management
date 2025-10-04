package vn.edu.fpt.cafemanagement.entities;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private int roleId;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL)
    private List<Manager> managers;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL)
    private List<Shift> shifts;

    @Column(name = "role_name")
    private String roleName;

}
