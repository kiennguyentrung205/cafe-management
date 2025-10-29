package vn.edu.fpt.cafemanagement.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.fpt.cafemanagement.services.ShiftScheduleService;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/schedule")
public class ScheduleController {
    @Autowired
    private ShiftScheduleService shiftScheduleService;

    @GetMapping
    public String viewSchedule(Model model,
                               @RequestParam("year")int year,
                               @RequestParam("weekRange") String weekRange) {
        String[] parts = weekRange.split(" To ");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid weekRange format: " + weekRange);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");


        MonthDay startMonthDay = MonthDay.parse(parts[0], formatter);
        MonthDay endMonthDay = MonthDay.parse(parts[1], formatter);


        LocalDate startDate = startMonthDay.atYear(year);
        LocalDate endDate = endMonthDay.atYear(year);

        // Nếu tuần băng qua năm sau (vd 30/12 -> 05/01)
        if (endDate.isBefore(startDate)) {
            endDate = endDate.plusYears(1);
        }

        Map<String, Map<String, Map<LocalDate, List<String>>>> schedule =
                shiftScheduleService.getScheduleWithAllDates(startDate, endDate);

        // DEBUG
        System.out.println("=== SCHEDULE DATA ===");
        schedule.forEach((shift, roleMap) -> {
            System.out.println("Shift: " + shift);
            roleMap.forEach((role, dateMap) -> {
                System.out.println("  Role: " + role);
                dateMap.forEach((date, managers) -> {
                    System.out.println("    Date: " + date + " -> " + managers);
                });
            });
        });

        model.addAttribute("schedule", schedule);
        model.addAttribute("dates", startDate.datesUntil(endDate.plusDays(1)).toList());

        model.addAttribute("currentYear", LocalDate.now().getYear());

        return "schedule-view";
    }

}

