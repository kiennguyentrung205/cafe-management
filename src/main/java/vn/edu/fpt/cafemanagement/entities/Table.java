package vn.edu.fpt.cafemanagement.entities;

import jakarta.persistence.*;

@Entity
public class Table {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "table_id", nullable = false)
    private int tableId;

    private String status;

    public Table(String status, int tableId) {
        this.status = status;
        this.tableId = tableId;
    }

    public Table() {
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
}
