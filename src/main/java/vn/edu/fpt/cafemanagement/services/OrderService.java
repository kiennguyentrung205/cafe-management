package vn.edu.fpt.cafemanagement.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import vn.edu.fpt.cafemanagement.entities.Order;
import vn.edu.fpt.cafemanagement.entities.OrderItem;
import vn.edu.fpt.cafemanagement.repositories.OrderItemRepository;
import vn.edu.fpt.cafemanagement.repositories.OrderRepository;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public void saveOrder(Order order) {
        orderRepository.save(order);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(int id) {
        return orderRepository.findById(id);
    }

    public void deleteOrder(int id) {
        orderRepository.deleteById(id);
    }

    public Page<Order> getPagedOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return orderRepository.findAll(pageable);
    }

    public List<OrderItem> getOrderItemsByOrderId(int orderId) {
        return orderItemRepository.findByOrder_OrderId(orderId);
    }

    // Lấy đơn đang hoạt động
    public Page<Order> getActiveOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return orderRepository.findActiveOrders(pageable);
    }

    // Lấy đơn lịch sử (Served, Canceled)
    public Page<Order> getHistoryOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return orderRepository.findHistoryOrders(pageable);
    }

    public Page<Order> getUnservedOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        List<String> excludedStatuses = List.of("Served", "Canceled");
        return orderRepository.findByStatusNotIn(excludedStatuses, pageable);
    }

}
