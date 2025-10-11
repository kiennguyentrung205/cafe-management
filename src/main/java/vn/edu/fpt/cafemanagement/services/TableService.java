package vn.edu.fpt.cafemanagement.services;

import org.springframework.stereotype.Service;
import vn.edu.fpt.cafemanagement.entities.Table;
import vn.edu.fpt.cafemanagement.repositories.TableRepository;

import java.util.List;

@Service
public class TableService {
    private final TableRepository tableRepository;
    public TableService(TableRepository tableRepository) {
        this.tableRepository = tableRepository;
    }
    public List<Table> getTablesList(){
        return tableRepository.getTablesList();
    }

    public Table findById(int id){
        return tableRepository.findById(id).orElse(null);
    }
}
