package vn.edu.fpt.cafemanagement.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import vn.edu.fpt.cafemanagement.entities.Manager;
import vn.edu.fpt.cafemanagement.entities.Order;
import vn.edu.fpt.cafemanagement.entities.OrderItem;
import vn.edu.fpt.cafemanagement.repositories.OrderItemRepository;
import vn.edu.fpt.cafemanagement.repositories.OrderRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderService {

    private OrderRepository orderRepository;
    private OrderItemRepository orderItemRepository;

    public OrderService(OrderRepository orderRepository,  OrderItemRepository orderItemRepository) {
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
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("orderId").descending());
        return orderRepository.findHistoryOrders(pageable);
    }

    public Page<Order> getUnservedOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        List<String> excludedStatuses = List.of("Served", "Canceled");
        return orderRepository.findByStatusNotIn(excludedStatuses, pageable);
    }

    /**
     * Lấy báo cáo sales và trả về dưới dạng Map.
     */
    public Map<String, Object> getSalesSummaryAsMap(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> summaryMap = new HashMap<>();

        if (startDate == null || endDate == null) {
            // Trả về Map rỗng nếu không có ngày
            return createEmptySummaryMap();
        }
        LocalDateTime startDateTime = startDate.atStartOfDay();

        // endDate = 07/10 -> 08/10 00:00:00 (để query < 08/10)
        LocalDateTime adjustedEndDateTime = endDate.plusDays(1).atStartOfDay();

        List<Object[]> results = orderRepository.getSalesSummaryObject(startDateTime, adjustedEndDateTime);

        if (!results.isEmpty()) {
            Object[] row = results.get(0);

            double totalRevenue = ((Number) row[0]).doubleValue();
            long totalOrders = ((Number) row[1]).longValue();
            long vouchersUsed = ((Number) row[2]).longValue();
            double averageOrderValue = totalOrders > 0 ? totalRevenue / totalOrders : 0;
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalRevenue", totalRevenue);
            summary.put("totalOrders", totalOrders);
            summary.put("vouchersUsed", vouchersUsed);
            summary.put("averageOrderValue", averageOrderValue);
            return summary;
        }
        return Collections.emptyMap();
    }

    // Hàm tiện ích
    private Map<String, Object> createEmptySummaryMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("totalRevenue", 0.0);
        map.put("totalOrders", 0L);
        map.put("vouchersUsed", 0L);
        map.put("averageOrderValue", 0.0);
        return map;
    }

    public List<Order> getOrdersByPeriod(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime adjustedEndDateTime = endDate.plusDays(1).atStartOfDay();
        return orderRepository.findAllByCreatedAtGreaterThanEqualAndCreatedAtLessThan(startDateTime, adjustedEndDateTime);
    }

    public Page<Order> getServedOrCanceledOrders(int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("orderId").descending());
        return orderRepository.findByStatusIn(List.of("Served", "Canceled"), pageable);
    }

    public void updateOrder(Order order, Manager currentManager) {
        order.setUpdatedAt(LocalDateTime.now());
        order.setUpdatedBy(currentManager);
        orderRepository.save(order);
    }
}
