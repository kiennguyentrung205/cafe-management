package vn.edu.fpt.cafemanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.cafemanagement.entities.PointHistory;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Integer> {
}
