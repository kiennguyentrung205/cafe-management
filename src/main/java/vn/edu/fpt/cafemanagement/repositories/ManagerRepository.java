package vn.edu.fpt.cafemanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.cafemanagement.entities.Manager;

import java.util.List;
import java.util.Optional;

@Repository
public interface ManagerRepository extends JpaRepository<Manager, Integer> {

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);

    Optional<Manager> findByUsername(String username);
    Optional<Manager> findByEmail(String email);
    Optional<Manager> findByPhoneNumber(String phoneNumber);
    @Query("SELECT m FROM Manager m LEFT JOIN m.role r WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(m.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(r.roleName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Manager> search(@Param("keyword") String keyword);

    // Lấy danh sách nhân viên còn active
    List<Manager> findByIsActiveTrue();

    // Lấy danh sách nhân viên đã xóa
    List<Manager> findByIsActiveFalse();


}
