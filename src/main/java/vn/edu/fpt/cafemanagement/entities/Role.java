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

    public Role() {
    }

    public Role(int roleId, List<Manager> managers, List<Shift> shifts, String roleName) {
        this.roleId = roleId;
        this.managers = managers;
        this.shifts = shifts;
        this.roleName = roleName;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public List<Manager> getManagers() {
        return managers;
    }

    public void setManagers(List<Manager> managers) {
        this.managers = managers;
    }

    public List<Shift> getShifts() {
        return shifts;
    }

    public void setShifts(List<Shift> shifts) {
        this.shifts = shifts;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
