package vn.edu.fpt.cafemanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.cafemanagement.entities.Manager;

@Repository
public interface ManagerRepository extends JpaRepository<Manager, Integer> {
}
