package vn.edu.fpt.cafemanagement.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.cafemanagement.entities.Shift;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShiftRepository extends JpaRepository<Shift,Integer> {

    List<Shift> getShiftByShiftDate(LocalDate shiftDate);

    Shift getShiftByShiftId(int id);
}
