package vn.edu.fpt.cafemanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.cafemanagement.entities.Manager;

public interface ManagerRepository extends JpaRepository<Manager, Integer> {
}
