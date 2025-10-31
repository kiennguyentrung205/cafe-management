package vn.edu.fpt.cafemanagement.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vn.edu.fpt.cafemanagement.repositories.CustomerRepository;
import vn.edu.fpt.cafemanagement.repositories.ManagerRepository;
import vn.edu.fpt.cafemanagement.entities.CustomUserDetails;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return customerRepository.findByUsername(username).<UserDetails>map(CustomUserDetails::new)
                .orElseGet(() ->
                managerRepository.findByUsername(username).map(CustomUserDetails::new)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username)));
    }

    private final CustomerRepository customerRepository;
    private final ManagerRepository managerRepository;

    public CustomUserDetailsService(CustomerRepository customerRepository, ManagerRepository managerRepository) {
        this.customerRepository = customerRepository;
        this.managerRepository = managerRepository;
    }



}
