package vn.edu.fpt.cafemanagement.security.models;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import vn.edu.fpt.cafemanagement.entities.Customer;
import vn.edu.fpt.cafemanagement.entities.Manager;

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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = (customer != null) ? "CUSTOMER" : manager.getRole().getRoleName();
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
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
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
