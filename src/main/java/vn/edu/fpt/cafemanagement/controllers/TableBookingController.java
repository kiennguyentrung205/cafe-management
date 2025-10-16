package vn.edu.fpt.cafemanagement.controllers;

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
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping(value = "/table/booking")
public class TableBookingController {
    private final LoggedUser loggedUser;
    private final TableService tableService;
    private final TableBookingService tableBookingService;
    private final CustomerService customerService;

    public TableBookingController(TableService tableService,  LoggedUser loggedUser,  TableBookingService tableBookingService, CustomerService customerService) {
        this.tableService = tableService;
        this.loggedUser = loggedUser;
        this.tableBookingService = tableBookingService;
        this.customerService = customerService;
    }

    @GetMapping(value="/view-history")
    public String showHistory(Model model) {
        Customer loggedCustomer = loggedUser.getLoggedCustomer();
        List<TableBooking> tableBooking = tableBookingService.findByCustomerId(loggedCustomer.getCusId());

        for (TableBooking tb : tableBooking) {
            System.out.println(tb.getBookingId());
        }

        System.out.println("LOi");

        model.addAttribute("tableBooking", tableBooking);
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

    @PostMapping(path="/new")
    public String addBooking(Model model, @ModelAttribute TableBooking tableBooking, RedirectAttributes redirectAttributes) {
        Customer loggedCustomer = loggedUser.getLoggedCustomer();
        tableBooking.setCustomer(loggedCustomer);

        LocalDateTime bookingTime = tableBooking.getBookingTime();
        LocalDateTime now = LocalDateTime.now();


// 2️⃣ Chỉ được đặt trong vòng 2 tiếng trước khi tới
        long diffMinutes = Duration.between(now, bookingTime).toMinutes();
        if (diffMinutes < 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "You cannot book table in the past!");
            return "redirect:/table/booking/new?table-id=" + tableBooking.getTable().getTableId();
        }

        if (diffMinutes > 120) {
            redirectAttributes.addFlashAttribute("errorMessage", "You can only book a table within 2 hours before your arrival");
            return "redirect:/table/booking/new?table-id=" + tableBooking.getTable().getTableId();
        }

// 3️⃣ Không được đặt sau 22 giờ
        if (bookingTime.getHour() >= 22) {
            redirectAttributes.addFlashAttribute("errorMessage", "You cannot book table after 22:00!");
            return "redirect:/table/booking/new?table-id=" + tableBooking.getTable().getTableId();
        }

        tableBookingService.save(tableBooking);
        tableService.updateStatus(tableBooking.getTable().getTableId(), "unavailable");

        redirectAttributes.addFlashAttribute("successMessage", "Table booking has been saved successfully!");

        return "redirect:/table/list?book-success";
    }
}
