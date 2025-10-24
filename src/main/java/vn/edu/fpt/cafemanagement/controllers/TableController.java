package vn.edu.fpt.cafemanagement.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.cafemanagement.entities.Table;
import vn.edu.fpt.cafemanagement.security.LoggedUser;
import vn.edu.fpt.cafemanagement.services.TableService;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/table")
public class TableController {
    private final LoggedUser loggedUser;
    private final TableService tableService;

    public TableController(TableService tableService, LoggedUser loggedUser) {
        this.tableService = tableService;
        this.loggedUser = loggedUser;
    }

    @GetMapping(path = "/list")
    public String showTableList(Model model) {
        List<Table> tables = tableService.getTablesList();
        model.addAttribute("tables", tables);
        return "table/table-list";
    }

    @GetMapping(path = "/management")
    @PreAuthorize("hasAnyRole('CASHIER', 'WAITER')")
    public String showTableListForStaff(Model model,
                                        @RequestParam(required = false) String status,
                                        @RequestParam(required = false) Integer capacity) {
        List<Table> tables = tableService.getTablesList();

        List<Integer> capacityList = tableService.getCapacityList();

        if (status != null && !status.isEmpty()) {
            tables = tables.stream()
                    .filter(t -> t.getStatus().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
        }

        if(capacity != null) {
            tables = tables.stream().filter(
                    t -> t.getCapacity() == capacity)
                    .collect(Collectors.toList());
        }
        model.addAttribute("status", status);
        model.addAttribute("capacity", capacity);
        model.addAttribute("tables", tables);
        model.addAttribute("capacityList", capacityList);

        return "staff/table/table-list-staff";
    }

    @PostMapping(path = "/management/update-status")
    @ResponseBody
    public String updateTableStatus(@RequestBody Table table) {
//        System.out.println(table.getStatus());
        tableService.updateTableStatus(table.getTableId(), table.getStatus());
        return "Status updated successfully";
    }

}
