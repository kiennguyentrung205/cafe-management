//package vn.edu.fpt.cafemanagement.util;
//
//import org.springframework.boot.context.event.ApplicationReadyEvent;
//import org.springframework.context.event.EventListener;
//import org.springframework.stereotype.Component;
//import vn.edu.fpt.cafemanagement.entities.TableBooking;
//import vn.edu.fpt.cafemanagement.repositories.TableBookingRepository;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Component
//public class BookingStartupChecker {
//    private TableBookingRepository tableBookingRepository;
//
//    public BookingStartupChecker(TableBookingRepository tableBookingRepository) {
//        this.tableBookingRepository = tableBookingRepository;
//    }
//
//    @EventListener(ApplicationReadyEvent.class)
//    public void checkExpiredBookingsOnStartup() {
//        LocalDateTime now = LocalDateTime.now();
//
//        List<TableBooking> expiredBookings = tableBookingRepository.findExpiredBooking(now.minusMinutes(15));
//
//        for (TableBooking x : expiredBookings) {
//            x.setStatus("auto-canceled");
//            tableBookingRepository.save(x);
//        }
//    }
//}
