package vn.edu.fpt.cafemanagement.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.cafemanagement.entities.CustomUserDetails;
import vn.edu.fpt.cafemanagement.entities.Manager;
import vn.edu.fpt.cafemanagement.repositories.ManagerRepository;

@Service
public class StaffUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(StaffUserDetailsService.class);

    private final ManagerRepository managerRepository;

    public StaffUserDetailsService(ManagerRepository managerRepository) {
        this.managerRepository = managerRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            Manager manager = managerRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        return new UsernameNotFoundException("Manager not found with username: " + username);
                    });

            return new CustomUserDetails(manager);
        } catch (Exception e) {
            throw e;
        }
    }
}