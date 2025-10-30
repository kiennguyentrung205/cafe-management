package vn.edu.fpt.cafemanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.cafemanagement.dto.ShiftScheduleDTO;
import vn.edu.fpt.cafemanagement.entities.ShiftAssignment;
import vn.edu.fpt.cafemanagement.entities.ShiftAssignmentId;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, ShiftAssignmentId> {

    @Query("""
        SELECT new vn.edu.fpt.cafemanagement.dto.ShiftScheduleDTO(
            s.shiftPeriod, r.roleName, s.shiftDate, m.name
        )
        FROM ShiftAssignment sa
        JOIN sa.shift s
        JOIN sa.manager m
        JOIN sa.role r
        WHERE s.shiftDate BETWEEN :start AND :end
        ORDER BY s.shiftDate, s.shiftPeriod, r.roleName
    """)
    List<ShiftScheduleDTO> findScheduleBetween(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
}

