package vn.edu.fpt.cafemanagement.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.cafemanagement.entities.Customer;
import vn.edu.fpt.cafemanagement.entities.Table;
import vn.edu.fpt.cafemanagement.entities.TableBooking;
import vn.edu.fpt.cafemanagement.security.LoggedUser;
import vn.edu.fpt.cafemanagement.services.CustomerService;
import vn.edu.fpt.cafemanagement.services.TableBookingService;
import vn.edu.fpt.cafemanagement.services.TableService;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping(value = "/table/booking")
public class TableBookingController {
    private final LoggedUser loggedUser;
    private final TableService tableService;
    private final TableBookingService tableBookingService;
    private final CustomerService customerService;

    public TableBookingController(TableService tableService, LoggedUser loggedUser, TableBookingService tableBookingService, CustomerService customerService) {
        this.tableService = tableService;
        this.loggedUser = loggedUser;
        this.tableBookingService = tableBookingService;
        this.customerService = customerService;
    }

    @GetMapping(value = "/my")
    public String showHistory(Model model,
                              @RequestParam(value = "page", defaultValue = "1") int page,
                              @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                              @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                              @RequestParam(value = "status", required = false) String status) {
        Customer loggedCustomer = loggedUser.getLoggedCustomer();

        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;
        if (startDate != null && endDate != null) {
            startDateTime = startDate.atStartOfDay();
            endDateTime = endDate.atTime(23, 59, 59);
        }


        // Pagination
        int size = 10;
        if (page < 1) {
            page = 1;
        }

        int pageIndex = Math.max(page - 1, 0);
        Pageable pageable = PageRequest.of(pageIndex, size);

        Page<TableBooking> tableBooking = tableBookingService.findByBookingTimeBetween(loggedCustomer.getCusId(), status, startDateTime, endDateTime, pageable);

        if (page > tableBooking.getTotalPages()) {
            page = tableBooking.getTotalPages();
            pageIndex = Math.max(page - 1, 0);
            pageable = PageRequest.of(pageIndex, size);
            tableBooking = tableBookingService.findByBookingTimeBetween(loggedCustomer.getCusId(), status, startDateTime, endDateTime, pageable);
        }
//        List<TableBooking> tableBooking = tableBookingService.findByCustomerId(loggedCustomer.getCusId());

        List<String> bookingStatus = new ArrayList<>(Arrays.asList("booked", "canceled", "checked-in"));
        int totalPages = Math.max(tableBooking.getTotalPages(), 1);


        model.addAttribute("bookingStatus", bookingStatus);
        model.addAttribute("tableBooking", tableBooking);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        return "table-booking/view-table-booking";
    }

    @GetMapping(path = "/new")
    public String showBookingTable(Model model, @RequestParam("table-id") int tableId) {

        if (model.containsAttribute("errorMessage")) {
            System.out.println("🔥 Có lỗi: " + model.getAttribute("errorMessage"));
        }

        Customer loggedCustomer = loggedUser.getLoggedCustomer();
        Table table = tableService.findById(tableId);
        TableBooking tableBooking = new TableBooking();
        tableBooking.setTable(table);

        tableBooking.setCustomer(loggedCustomer);

        model.addAttribute("now", LocalDate.now());
        model.addAttribute("tableBooking", tableBooking);
        model.addAttribute("table", table);
        model.addAttribute("loggedCustomer", loggedCustomer);

        return "table-booking/create-booking";
    }

    @PostMapping(path = "/new")
    public String addBooking(Model model, @ModelAttribute TableBooking tableBooking, RedirectAttributes redirectAttributes) {
        Customer loggedCustomer = loggedUser.getLoggedCustomer();
        tableBooking.setCustomer(loggedCustomer);

        LocalDateTime bookingTime = tableBooking.getBookingTime();
        LocalDateTime now = LocalDateTime.now();


        long diffMinutes = Duration.between(now, bookingTime).toMinutes();
        if (diffMinutes < 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "You cannot book table in the past!");
            return "redirect:/table/booking/new?table-id=" + tableBooking.getTable().getTableId();
        }

        if (diffMinutes > 120) {
            redirectAttributes.addFlashAttribute("errorMessage", "You can only book a table within 2 hours before your arrival");
            return "redirect:/table/booking/new?table-id=" + tableBooking.getTable().getTableId();
        }

        if (bookingTime.getHour() >= 22) {
            redirectAttributes.addFlashAttribute("errorMessage", "You cannot book table after 22:00!");
            return "redirect:/table/booking/new?table-id=" + tableBooking.getTable().getTableId();
        }

        tableBooking.setStatus("booked");

        tableBookingService.save(tableBooking);
        tableService.updateStatus(tableBooking.getTable().getTableId(), "booked");

        redirectAttributes.addFlashAttribute("successMessage", "Table booking has been saved successfully!");

        return "redirect:/table/list?book-success";
    }


    @PostMapping(value = "/cancel")
    public String cancelBooking(Model model, @RequestParam("tableBookingId") int bookingId, RedirectAttributes redirectAttributes) {
        Customer loggedCustomer = loggedUser.getLoggedCustomer();

        TableBooking tableBooking = tableBookingService.findById(bookingId);
        if (tableBooking.getCustomer().getCusId() != loggedCustomer.getCusId()) {
            return "redirect:/table/booking/my?cancel=failed";
        }

        tableService.updateStatus(tableBooking.getTable().getTableId(), "available");

        tableBooking.setStatus("canceled");
        tableBookingService.save(tableBooking);

        return "redirect:/table/booking/my?cancel=success";
    }

    @GetMapping(value = "/management")
    public String showBookingManagement(Model model,
                                        @RequestParam(value = "page", defaultValue = "1") int page,
                                        @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                        @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                        @RequestParam(value = "status", required = false) String status) {

        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;
        if (startDate != null && endDate != null) {
            startDateTime = startDate.atStartOfDay();
            endDateTime = endDate.atTime(23, 59, 59);
        }


        // Pagination
        int size = 10;
        if (page < 1) {
            page = 1;
        }

        int pageIndex = Math.max(page - 1, 0);
        Pageable pageable = PageRequest.of(pageIndex, size);

        Page<TableBooking> tableBooking = tableBookingService.findByStatusAndDateBetween( status, startDateTime, endDateTime, pageable);

        if (page > tableBooking.getTotalPages()) {
            page = tableBooking.getTotalPages();
            pageIndex = Math.max(page - 1, 0);
            pageable = PageRequest.of(pageIndex, size);
            tableBooking = tableBookingService.findByStatusAndDateBetween(status, startDateTime, endDateTime, pageable);
        }
//        List<TableBooking> tableBooking = tableBookingService.findByCustomerId(loggedCustomer.getCusId());

        List<String> bookingStatus = new ArrayList<>(Arrays.asList("booked", "canceled", "checked-in"));

        int totalPages = Math.max(tableBooking.getTotalPages(), 1);
        System.out.println(totalPages);


        model.addAttribute("bookingStatus", bookingStatus);
        model.addAttribute("tableBooking", tableBooking);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        return "staff/table-booking/staff-table-booking-history";
    }

    @PostMapping(value="/management/checkin")
    public String updateStatus(Model model, @RequestParam("tableBookingId") int bookingId, RedirectAttributes redirectAttributes) {
        TableBooking booking = tableBookingService.findById(bookingId);
        if(booking.getStatus().equals("canceled")) {
            return "redirect:/table/booking/management?update=failed";
        }
        booking.setStatus("checked-in");
        tableBookingService.save(booking);
        return "redirect:/table/booking/management?update=success";
    }
}
