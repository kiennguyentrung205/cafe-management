package vn.edu.fpt.cafemanagement.entities;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private int roleId;

    @Column(name = "role_name", nullable = false, length = 50)
    private String roleName;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL)
    private List<Manager> manager;

//    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL)
//    private List<Shift> shifts;

//    @ManyToMany(mappedBy = "shiftRoles")
//    private List<Shift> shiftsRoles;

    @ManyToMany(mappedBy = "requiredRoles")
    private List<Shift> shifts;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL)
    private List<ShiftAssignment> assignments;

    public Role() {
    }

    public Role(int roleId, String roleName, List<Manager> manager, List<Shift> shifts, List<Shift> shiftsRoles) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.manager = manager;
//        this.shifts = shifts;
//        this.shiftsRoles = shiftsRoles;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public List<Manager> getManager() {
        return manager;
    }

    public void setManager(List<Manager> manager) {
        this.manager = manager;
    }

//    public List<Shift> getShifts() {
//        return shifts;
//    }
//
//    public void setShifts(List<Shift> shifts) {
//        this.shifts = shifts;
//    }

//    public List<Shift> getShiftsRoles() {
//        return shiftsRoles;
//    }
//
//    public void setShiftsRoles(List<Shift> shiftsRoles) {
//        this.shiftsRoles = shiftsRoles;
//    }


    public List<ShiftAssignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<ShiftAssignment> assignments) {
        this.assignments = assignments;
    }

    public List<Shift> getShifts() {
        return shifts;
    }

    public void setShifts(List<Shift> shifts) {
        this.shifts = shifts;
    }
}
