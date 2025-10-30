package vn.edu.fpt.cafemanagement.entities;

import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class ShiftAssignmentId implements Serializable {
    private int shiftId;
    private int managerId;
    private int roleId;

    // equals() và hashCode() là bắt buộc
}
