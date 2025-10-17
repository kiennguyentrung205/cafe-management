package vn.edu.fpt.cafemanagement.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.cafemanagement.entities.TableBooking;
import vn.edu.fpt.cafemanagement.repositories.TableBookingRepository;
import vn.edu.fpt.cafemanagement.repositories.TableRepository;

import java.util.List;

@Service
public class TableBookingService {
    private final TableBookingRepository tableBookingRepository;

    public TableBookingService(TableBookingRepository tableBookingRepository) {
        this.tableBookingRepository = tableBookingRepository;
    }

    @Transactional
    public TableBooking save(TableBooking tableBooking) {
        return tableBookingRepository.save(tableBooking);
    }

    public List<TableBooking> findByCustomerId(int customerId) {
        return tableBookingRepository.findByCustomer_CusId(customerId);
    }

    public TableBooking findById(int id) {
        return tableBookingRepository.findById(id).orElse(null);
    }
}
