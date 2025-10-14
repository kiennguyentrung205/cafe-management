package vn.edu.fpt.cafemanagement.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.cafemanagement.entities.Customer;
import vn.edu.fpt.cafemanagement.entities.Table;
import vn.edu.fpt.cafemanagement.entities.TableBooking;
import vn.edu.fpt.cafemanagement.security.LoggedUser;
import vn.edu.fpt.cafemanagement.services.CustomerService;
import vn.edu.fpt.cafemanagement.services.TableBookingService;
import vn.edu.fpt.cafemanagement.services.TableService;

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

        Customer loggedCustomer = loggedUser.getLoggedCustomer();
        Table table = tableService.findById(tableId);
        TableBooking tableBooking = new TableBooking();
        tableBooking.setTable(table);

        tableBooking.setCustomer(loggedCustomer);

        model.addAttribute("tableBooking", tableBooking);
        model.addAttribute("table", table);
        model.addAttribute("loggedCustomer", loggedCustomer);

        return "table-booking/create-booking";
    }

    @PostMapping(path="/new")
    public String addBooking(Model model, @ModelAttribute TableBooking tableBooking) {
        Customer loggedCustomer = loggedUser.getLoggedCustomer();
        tableBooking.setCustomer(loggedCustomer);

        tableBookingService.save(tableBooking);
        tableService.updateStatus(tableBooking.getTable().getTableId(), "unavailable");

        return "redirect:/table/list?book-success";
    }
}
