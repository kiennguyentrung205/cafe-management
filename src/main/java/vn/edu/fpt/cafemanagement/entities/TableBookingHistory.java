package vn.edu.fpt.cafemanagement.entities;

import jakarta.persistence.*;


@Entity
public class TableBookingHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_history_id")
    private int bookingHistoryId;

    @ManyToOne
    @JoinColumn(name = "table_id", nullable = false)
    private Table table;

    @ManyToOne
    @JoinColumn(name = "cus_id", nullable = false)
    private Customer customer;

    public TableBookingHistory() {
    }

    public TableBookingHistory(int bookingHistoryId, Table table, Customer customer) {
        this.bookingHistoryId = bookingHistoryId;
        this.table = table;
        this.customer = customer;
    }

    public int getBookingHistoryId() {
        return bookingHistoryId;
    }

    public void setBookingHistoryId(int bookingHistoryId) {
        this.bookingHistoryId = bookingHistoryId;
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
}
