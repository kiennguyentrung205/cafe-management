package vn.edu.fpt.cafemanagement.repositories;

import jakarta.persistence.*;

@Entity
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private int roleId;

    @Column(name = "role_name", nullable = false, length = 50)
    private String roleName;

//    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL)
//    private List<Manager> managers;
//
//    public Role() {}
//
//    public Role(int roleId, String roleName) {
//        this.roleId = roleId;
//        this.roleName = roleName;
//    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

//    public List<Manager> getManagers() {
//        return managers;
//    }
//
//    public void setManagers(List<Manager> managers) {
//        this.managers = managers;
//    }
}
