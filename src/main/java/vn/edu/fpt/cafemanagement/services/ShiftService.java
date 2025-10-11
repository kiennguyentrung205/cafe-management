package vn.edu.fpt.cafemanagement.services;

import org.springframework.stereotype.Service;
import vn.edu.fpt.cafemanagement.entities.Shift;
import vn.edu.fpt.cafemanagement.repositories.ShiftRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class ShiftService {
    private ShiftRepository shiftRepository;
    public ShiftService(ShiftRepository shiftRepository) {
        this.shiftRepository = shiftRepository;
    }

    public List<Shift> getShiftByDate(LocalDate date){
        return shiftRepository.getShiftByShiftDate(date);
    }

    public Shift getShiftById(int id){
        return shiftRepository.getShiftByShiftId(id);
    }
}
