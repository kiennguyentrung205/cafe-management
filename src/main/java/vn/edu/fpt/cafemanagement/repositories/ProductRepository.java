package vn.edu.fpt.cafemanagement.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.cafemanagement.entities.Product;

import java.util.List;
@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    List<Product> findByIsActiveTrue();

    List<Product> findByIsActiveTrueAndCategoryCateId(Integer categoryId);

    Page<Product> findByIsActiveTrue(Pageable pageable);    //sửa tên hàm cho đúng với tên biến bên entity
}