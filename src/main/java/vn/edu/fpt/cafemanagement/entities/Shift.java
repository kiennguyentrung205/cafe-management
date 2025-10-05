package vn.edu.fpt.cafemanagement.entities;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Entity
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shift_id")
    private int shiftId;

    @Column(name = "shift_date")
    private LocalDate shiftDate;

    @Column(name = "shift_period")
    private String shiftPeriod;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToMany(mappedBy = "shifts")
    private List<Manager> managers;

    public Shift() {
    }

    public Shift(int shiftId, LocalDate shiftDate, String shiftPeriod, Role role, LocalDateTime createdAt,
                 LocalDateTime updatedAt, List<Manager> managers) {
        this.shiftId = shiftId;
        this.shiftDate = shiftDate;
        this.shiftPeriod = shiftPeriod;
        this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.managers = managers;
    }

    public int getShiftId() {
        return shiftId;
    }

    public void setShiftId(int shiftId) {
        this.shiftId = shiftId;
    }

    public LocalDate getShiftDate() {
        return shiftDate;
    }

    public void setShiftDate(LocalDate shiftDate) {
        this.shiftDate = shiftDate;
    }

    public String getShiftPeriod() {
        return shiftPeriod;
    }

    public void setShiftPeriod(String shiftPeriod) {
        this.shiftPeriod = shiftPeriod;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Manager> getManagers() {
        return managers;
    }

    public void setManagers(List<Manager> managers) {
        this.managers = managers;
    }
}
