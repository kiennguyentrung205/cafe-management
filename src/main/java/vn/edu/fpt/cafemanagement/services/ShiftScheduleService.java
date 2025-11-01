package vn.edu.fpt.cafemanagement.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.fpt.cafemanagement.dto.ShiftScheduleDTO;
import vn.edu.fpt.cafemanagement.repositories.ShiftAssignmentRepository;

import java.time.LocalDate;
import java.util.*;

@Service
public class ShiftScheduleService {

    @Autowired
    private ShiftAssignmentRepository shiftAssignmentRepository;

    public Map<String, Map<String, Map<LocalDate, List<String>>>> getSchedule(LocalDate start, LocalDate end) {
        List<ShiftScheduleDTO> list = shiftAssignmentRepository.findScheduleBetween(start, end);

        // Map theo cấu trúc: ShiftPeriod → Role → Date → List<ManagerName>
        Map<String, Map<String, Map<LocalDate, List<String>>>> schedule = new LinkedHashMap<>();

        for (ShiftScheduleDTO dto : list) {
            schedule
                    .computeIfAbsent(dto.getShiftPeriod(), k -> new LinkedHashMap<>())
                    .computeIfAbsent(dto.getRoleName(), k -> new LinkedHashMap<>())
                    .computeIfAbsent(dto.getShiftDate(), k -> new ArrayList<>())
                    .add(dto.getManagerName());
        }

        return schedule;
    }

    // Version có đầy đủ các ngày (nếu cần)
    public Map<String, Map<String, Map<LocalDate, List<String>>>> getScheduleWithAllDates(LocalDate start, LocalDate end) {
        List<ShiftScheduleDTO> list = shiftAssignmentRepository.findScheduleBetween(start, end);

        // Danh sách tất cả các ngày
        List<LocalDate> allDates = start.datesUntil(end.plusDays(1)).toList();

        // Định nghĩa các shift và role (có thể lấy từ enum hoặc config)
        List<String> shifts = Arrays.asList("Morning", "Afternoon", "Evening");
        List<String> roles = Arrays.asList("Waiter", "Barista", "Cashier");

        Map<String, Map<String, Map<LocalDate, List<String>>>> schedule = new LinkedHashMap<>();

        // Khởi tạo cấu trúc đầy đủ
        for (String shift : shifts) {
            Map<String, Map<LocalDate, List<String>>> roleMap = new LinkedHashMap<>();
            for (String role : roles) {
                Map<LocalDate, List<String>> dateMap = new LinkedHashMap<>();
                for (LocalDate date : allDates) {
                    dateMap.put(date, new ArrayList<>());
                }
                roleMap.put(role, dateMap);
            }
            schedule.put(shift, roleMap);
        }
        for (ShiftScheduleDTO dto : list) {
            String shift = dto.getShiftPeriod();
            String role = dto.getRoleName();
            LocalDate date = dto.getShiftDate();

            if (!schedule.containsKey(shift)) {
                System.out.println("⚠ Unknown shift: " + shift);
                continue;
            }
            if (!schedule.get(shift).containsKey(role)) {
                System.out.println("⚠ Unknown role: " + role);
                continue;
            }
            if (!schedule.get(shift).get(role).containsKey(date)) {
                System.out.println("⚠ Date out of range: " + date);
                continue;
            }

            schedule.get(shift).get(role).get(date).add(dto.getManagerName());
        }


//        // Fill dữ liệu từ database
//        for (ShiftScheduleDTO dto : list) {
//            schedule
//                    .get(dto.getShiftPeriod())
//                    .get(dto.getRoleName())
//                    .get(dto.getShiftDate())
//                    .add(dto.getManagerName());
//        }

        return schedule;
    }
}
