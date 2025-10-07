package vn.edu.fpt.cafemanagement.entities;


import jakarta.persistence.*;
import jakarta.persistence.Table;

@Entity
@Table(name = "product")
public class Product {
    @Id
    @Column(name = "pro_id")
    private int proId;
    private String proName;
    private String img;
    private String status;
    private String description;
    @Column(name = "is_active")
    private boolean isActive;
    @ManyToOne
    @JoinColumn(name = "cate_id")
    private Category category;

    public Product() {
    }

    public Product(int proId, String proName, String img, String status, String description, boolean isActive, Category category) {
        this.proId = proId;
        this.proName = proName;
        this.img = img;
        this.status = status;
        this.description = description;
        this.isActive = isActive;
        this.category = category;
    }

    public int getProId() {
        return proId;
    }

    public void setProId(int pro_id) {
        this.proId = pro_id;
    }


    public String getProName() {
        return proName;
    }

    public void setProName(String proName) {
        this.proName = proName;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}