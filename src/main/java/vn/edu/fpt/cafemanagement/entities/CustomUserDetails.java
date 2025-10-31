package vn.edu.fpt.cafemanagement.entities;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {
    private final Customer customer;
    private final Manager manager;

    public CustomUserDetails(Customer customer) {
        this.customer = customer;
        this.manager = null;
    }

    public CustomUserDetails(Manager manager) {
        this.manager = manager;
        this.customer = null;
    }

    public boolean isCustomer() {
        return this.customer != null;
    }

    public boolean isManager() {
        return this.manager != null;
    }

    public Customer getCustomer() {
        return this.customer;
    }

    public Manager getManager() {
        return this.manager;
    }

    // Thêm method để lấy ID
    public Integer getId() {
        if (customer != null) {
            return customer.getCusId();
        }
        if (manager != null) {
            return manager.getManagerId();
        }
        return null;
    }

    // Thêm method để lấy email
    public String getEmail() {
        if (customer != null) {
            return customer.getEmail();
        }
        if (manager != null) {
            return manager.getEmail();
        }
        return null;
    }

    public String getFullName() {
        if (customer != null) {
            return customer.getName();
        }
        if (manager != null) {
            return manager.getName();
        }
        return null;
    }

    // Thêm method để lấy user type
    public String getUserType() {
        return customer != null ? "CUSTOMER" : "MANAGER";
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (customer != null) {
            return List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
        }
        if (manager != null) {
            String roleName = manager.getRole().getRoleName();
            return List.of(new SimpleGrantedAuthority("ROLE_" + roleName.toUpperCase()));
        }
        return List.of();
    }

    @Override
    public String getPassword() {
        return (customer != null) ? customer.getPassword() : manager.getPassword();
    }

    @Override
    public String getUsername() {
        return (customer != null) ? customer.getUsername() : manager.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        if (customer != null) {
            return customer.isEnabled(); // Nếu Customer có field enabled
        }
        if (manager != null) {
            return manager.isEnabled(); // Nếu Manager có field enabled
        }
        return true;
    }
}