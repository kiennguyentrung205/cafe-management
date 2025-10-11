package vn.edu.fpt.cafemanagement.services;

import org.springframework.stereotype.Service;
import vn.edu.fpt.cafemanagement.entities.Product;
import vn.edu.fpt.cafemanagement.repositories.ProductRepository;

import java.util.List;

@Service
public class ProductService {
    private ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
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
        return productRepository.findByIsActiveTrueAndCategoryCateIdAndCategoryIsActiveTrue(categoryId);

    }

    public Product getProductById(int productId) {
        return productRepository.findById(productId).get();
    }


    public void deleteSortProduct(Product product) {
        product.setActive(false);
        productRepository.save(product);
    }
}