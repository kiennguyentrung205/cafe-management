package vn.edu.fpt.cafemanagement.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class TableBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private int bookingId;

    @ManyToOne
    @JoinColumn(name = "table_id", nullable = false)
    private Table table;

    @ManyToOne
    @JoinColumn(name = "cus_id", nullable = false)
    private Customer customer;

    //Cancel, Booked
    @Column(name = "status", length = 20)
    private String status;

    //Sau 22h không đc đặt bàn
    @Column(name = "booking_time")
    private LocalDateTime bookingTime;

    public TableBooking() {
    }

    public TableBooking(int bookingId, Table table, Customer customer, String status, LocalDateTime bookingTime) {
        this.bookingId = bookingId;
        this.table = table;
        this.customer = customer;
        this.status = status;
        this.bookingTime = bookingTime;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(LocalDateTime bookingTime) {
        this.bookingTime = bookingTime;
    }

    public String tosString(){
        return String.format("%d %s %s %s %s", table.getTableId(), customer.getName(), customer.getPhoneNumber(), status, bookingTime);
    }
}
