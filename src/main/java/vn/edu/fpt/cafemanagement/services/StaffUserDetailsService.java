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
        logger.info("=== STAFF LOGIN ATTEMPT ===");
        logger.info("Attempting to load user: {}", username);

        try {
            Manager manager = managerRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        logger.error("Manager not found with username: {}", username);
                        return new UsernameNotFoundException("Manager not found with username: " + username);
                    });

            logger.info("Manager found: {}", manager.getUsername());
            logger.info("Manager ID: {}", manager.getManagerId());
            logger.info("Manager Role: {}", manager.getRole() != null ? manager.getRole().getRoleName() : "NULL");
            logger.info("Password from DB: {}", manager.getPassword() != null ? "EXISTS" : "NULL");

            CustomUserDetails userDetails = new CustomUserDetails(manager);
            logger.info("CustomUserDetails created successfully");
            logger.info("Authorities: {}", userDetails.getAuthorities());

            return userDetails;
        } catch (Exception e) {
            logger.error("Error loading user: {}", e.getMessage(), e);
            throw e;
        }
    }
}