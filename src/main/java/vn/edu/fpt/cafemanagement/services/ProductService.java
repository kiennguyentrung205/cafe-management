package vn.edu.fpt.cafemanagement.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.edu.fpt.cafemanagement.entities.Order;
import vn.edu.fpt.cafemanagement.entities.Product;
import vn.edu.fpt.cafemanagement.repositories.OrderRepository;
import vn.edu.fpt.cafemanagement.repositories.ProductRepository;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public ProductService(ProductRepository productRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }


    public void saveProduct(Product product) {
        productRepository.save(product);
    }

    public void deleteProduct(Product product) {
        productRepository.delete(product);
    }

    public void updateProduct(Product product) {
        productRepository.save(product);
    }

    public List<Product> getActiveProducts() {
        return productRepository.findByIsActiveTrueAndCategoryIsActiveTrue();
    }

    public List<Product> getProductsByCategory(int categoryId) {
        return productRepository.findByIsActiveTrueAndCategoryCateId(categoryId);
    }

    public Product getProductById(int productId) {
        return productRepository.findById(productId).orElse(null);
    }

    public Page<Product> getActiveProductsPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return productRepository.findByIsActiveTrue(pageable);
    }

    // ---------------- ORDER LOGIC ----------------
    /**
     * Tính tổng phụ (subtotal) của danh sách sản phẩm
     * Dựa trên giá * số lượng
     */
    public double calculateSubtotal(List<Integer> productIds, List<Integer> quantities) {
        double subtotal = 0;
        for (int i = 0; i < productIds.size(); i++) {
            Product product = getProductById(productIds.get(i));
            if (product != null) {
                subtotal += product.getPrice() * quantities.get(i);
            }
        }
        return subtotal;
    }

    //Cập nhật tổng giá của đơn hàng
    public void saveOrderItems(Order order, List<Integer> productIds, List<Integer> quantities) {
        double subtotal = calculateSubtotal(productIds, quantities);
        order.setTotalPrice(subtotal);
        orderRepository.save(order);
    }

    public void deleteSortProduct(Product product) {
        product.setActive(false);
        productRepository.save(product);
    }
  
}
