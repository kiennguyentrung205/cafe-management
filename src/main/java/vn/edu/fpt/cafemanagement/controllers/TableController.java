package vn.edu.fpt.cafemanagement.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import vn.edu.fpt.cafemanagement.entities.Table;
import vn.edu.fpt.cafemanagement.security.LoggedUser;
import vn.edu.fpt.cafemanagement.services.TableService;

import java.util.List;

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
}
