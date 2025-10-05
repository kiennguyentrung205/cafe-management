package vn.edu.fpt.cafemanagement.entities;

import jakarta.persistence.*;

import java.util.List;


@Entity
public class Table {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "table_id")
    private int tableId;

    @Column(name = "status")
    private String status;

    @OneToMany(mappedBy = "table", cascade = CascadeType.ALL)
    private List<TableBookingHistory> tableBookingHistories;

    public Table() {
    }

    public Table(int tableId, String status, List<TableBookingHistory> tableBookingHistories) {
        this.tableId = tableId;
        this.status = status;
        this.tableBookingHistories = tableBookingHistories;
    }

    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<TableBookingHistory> getTableBookingHistories() {
        return tableBookingHistories;
    }

    public void setTableBookingHistories(List<TableBookingHistory> tableBookingHistories) {
        this.tableBookingHistories = tableBookingHistories;
    }
}
