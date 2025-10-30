package vn.edu.fpt.cafemanagement.entities;

import jakarta.persistence.*;
import jakarta.persistence.Table;

@Entity
@Table(name = "Shiftassignment")
public class ShiftAssignment {

    @EmbeddedId
    private ShiftAssignmentId id;

    @ManyToOne
    @MapsId("shiftId")
    @JoinColumn(name = "shift_id")
    private Shift shift;

    @ManyToOne
    @MapsId("managerId")
    @JoinColumn(name = "manager_id")
    private Manager manager;

    @ManyToOne
    @MapsId("roleId")
    @JoinColumn(name = "role_id")
    private Role role;

    public ShiftAssignmentId getId() {
        return id;
    }

    public void setId(ShiftAssignmentId id) {
        this.id = id;
    }

    public Shift getShift() {
        return shift;
    }

    public void setShift(Shift shift) {
        this.shift = shift;
    }

    public Manager getManager() {
        return manager;
    }

    public void setManager(Manager manager) {
        this.manager = manager;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}

