package vn.edu.fpt.cafemanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.cafemanagement.entities.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {
}
