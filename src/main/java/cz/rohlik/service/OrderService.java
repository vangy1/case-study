package cz.rohlik.service;

import cz.rohlik.exception.InsufficientStockException;
import cz.rohlik.exception.OrderNotFoundException;
import cz.rohlik.exception.ProductNotFoundException;
import cz.rohlik.job.OrderCancelIfNotPaidJob;
import cz.rohlik.mapper.OrderMapper;
import cz.rohlik.model.OrderCreateRequest;
import cz.rohlik.model.OrderItemInsufficient;
import cz.rohlik.model.OrderItemRequest;
import cz.rohlik.persistence.entity.Order;
import cz.rohlik.persistence.entity.OrderItem;
import cz.rohlik.persistence.entity.Product;
import cz.rohlik.persistence.repository.OrderItemRepository;
import cz.rohlik.persistence.repository.OrderRepository;
import cz.rohlik.persistence.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.StaleStateException;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;
    private final Scheduler scheduler;

    @Value("${rohlik.order.unpaid-cancellation-minutes}")
    private Integer unpaidCancellationMinutes;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    @Retryable(retryFor = StaleStateException.class)
    public Long createOrder(OrderCreateRequest orderCreateRequest) {
        log.info("Creating order with request: {}", orderCreateRequest);

        double totalPrice = 0;
        List<OrderItemInsufficient> missingItems = new ArrayList<>();
        for (OrderItemRequest orderItemRequest : orderCreateRequest.getOrderItems()) {
            Product product = productRepository.findByIdAndDeletedIsFalse(orderItemRequest.getProductId()).orElseThrow(() -> new ProductNotFoundException(orderItemRequest.getProductId()));

            long futureInventory = product.getInventory() - orderItemRequest.getQuantity();
            if (futureInventory < 0) {
                missingItems.add(orderMapper.toOrderItemInsufficient(orderItemRequest.getProductId(), futureInventory));
            }
            product.setInventory(futureInventory);
            totalPrice += product.getPrice() * orderItemRequest.getQuantity();
        }

        if (!missingItems.isEmpty()) {
            log.info("Insufficient stock for some items: {}", missingItems);
            throw new InsufficientStockException(missingItems);
        }

        Long orderId = createNewOrder(orderCreateRequest, totalPrice);
        scheduleCancellationCheck(orderId);

        log.info("Order created with ID: {}", orderId);
        return orderId;
    }

    private Long createNewOrder(OrderCreateRequest orderCreateRequest, double totalPrice) {
        Order order = new Order();
        order.setTotalPrice(totalPrice);

        List<OrderItem> orderItems = orderCreateRequest.getOrderItems().stream().map(orderItemRequest -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setQuantity(orderItemRequest.getQuantity());
            orderItem.setProduct(productRepository.findById(orderItemRequest.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(orderItemRequest.getProductId())));
            orderItem.setOrder(order);
            return orderItem;
        }).toList();
        orderItemRepository.saveAll(orderItems);
        order.setItems(orderItems);

        return orderRepository.save(order).getId();
    }


    private void scheduleCancellationCheck(Long orderId) {
        JobDetail jobDetail = JobBuilder.newJob(OrderCancelIfNotPaidJob.class)
                .setJobData(new JobDataMap(Collections.singletonMap("orderId", orderId)))
                .storeDurably()
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .startAt(DateBuilder.futureDate(20, DateBuilder.IntervalUnit.SECOND))
                .build();

        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            log.error("Failed to schedule cancellation check for order ID: {}", orderId, e);
            throw new RuntimeException(e);
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    @Retryable(retryFor = StaleStateException.class)
    public void cancelOrder(Long orderId) {
        log.info("Cancelling order with ID: {}", orderId);
        Order order = getOrderById(orderId);
        if(order.isCancelled()){
            log.info("Order with ID: {} is already cancelled", orderId);
            return;
        }
        order.getItems().forEach(orderItem -> {
            Product product = orderItem.getProduct();
            product.setInventory(product.getInventory() + orderItem.getQuantity());
        });
        order.setCancelled(true);
        log.info("Order with ID: {} has been cancelled", orderId);
    }

    @Transactional
    public void payOrder(Long orderId) {
        log.info("Marking order with ID: {} as paid", orderId);
        Order order = getOrderById(orderId);
        order.setPaid(true);
        log.info("Order with ID: {} has been marked as paid", orderId);
    }


    @Transactional(isolation = Isolation.SERIALIZABLE)
    @Retryable(retryFor = StaleStateException.class)
    public void cancelOrderIfNotPaid(Long orderId) {
        if(!getOrderById(orderId).isPaid()){
            log.info("Order with ID: {} not paid after {} minutes", orderId, unpaidCancellationMinutes);
            cancelOrder(orderId);
        }
    }

    private Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }
}
