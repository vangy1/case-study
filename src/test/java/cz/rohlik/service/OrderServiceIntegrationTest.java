package cz.rohlik.service;

import cz.rohlik.exception.InsufficientStockException;
import cz.rohlik.model.OrderCreateRequest;
import cz.rohlik.model.OrderItemInsufficient;
import cz.rohlik.model.OrderItemRequest;
import cz.rohlik.persistence.entity.Order;
import cz.rohlik.persistence.entity.OrderItem;
import cz.rohlik.persistence.entity.Product;
import cz.rohlik.persistence.repository.OrderItemRepository;
import cz.rohlik.persistence.repository.OrderRepository;
import cz.rohlik.persistence.repository.ProductInventoryRepository;
import cz.rohlik.persistence.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("it")
public class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductInventoryRepository productInventoryRepository;


    @AfterEach
    public void cleanUp() {
        productInventoryRepository.deleteAll();
        orderItemRepository.deleteAll();
        productRepository.deleteAll();
        orderRepository.deleteAll();
    }

    @Test
    @Transactional
    public void testCreateOrder_stockOk(){
        Product product1 = getMockProduct("Product1", 1.0, 10L);
        Product product2 = getMockProduct("Product2", 5.0, 10L);
        Product product3 = getMockProduct("Product3", 10.0, 10L);
        productRepository.saveAll(List.of(product1,product2,product3));
        OrderCreateRequest request = new OrderCreateRequest().orderItems(List.of(
                new OrderItemRequest().productId(product1.getId()).quantity(3L),
                new OrderItemRequest().productId(product2.getId()).quantity(6L),
                new OrderItemRequest().productId(product3.getId()).quantity(10L)
        ));

        Long orderId = orderService.createOrder(request);

        assertTrue(orderRepository.findById(orderId).isPresent());
        Order order = orderRepository.findById(orderId).get();
        assertFalse(order.isPaid());
        assertFalse(order.isCancelled());
        assertEquals(7, product1.getInventory());
        assertEquals(4, product2.getInventory());
        assertEquals(0, product3.getInventory());
        assertEquals(133, order.getTotalPrice());
    }

    @Test
    public void testCreateOrder_stockNotOk(){
        Product product1 = getMockProduct("Product1", 1.0, 10L);
        Product product2 = getMockProduct("Product2", 5.0, 10L);
        Product product3 = getMockProduct("Product3", 10.0, 10L);
        productRepository.saveAll(List.of(product1,product2,product3));
        OrderCreateRequest request = new OrderCreateRequest().orderItems(List.of(
                new OrderItemRequest().productId(product1.getId()).quantity(15L),
                new OrderItemRequest().productId(product2.getId()).quantity(12L),
                new OrderItemRequest().productId(product3.getId()).quantity(10L)
        ));

        List<OrderItemInsufficient> orderItemInsufficientList =
                assertThrows(InsufficientStockException.class,
                        () -> orderService.createOrder(request)).getOrderItemInsufficientList();

        assertEquals(2, orderItemInsufficientList.size());
        assertEquals(-5, orderItemInsufficientList.get(0).getMissingQuantity());
        assertEquals(-2, orderItemInsufficientList.get(1).getMissingQuantity());
    }

    @Test
    @Transactional
    public void testCreateAndCancelOrderInventory() {
        Product product = getMockProduct("Product", 1.0, 10L);
        productRepository.save(product);
        OrderCreateRequest request = new OrderCreateRequest().orderItems(List.of(
                new OrderItemRequest().productId(product.getId()).quantity(3L)
        ));

        Long orderId = orderService.createOrder(request);

        assertEquals(7L, product.getInventory());

        orderService.cancelOrder(orderId);

        assertTrue(orderRepository.findById(orderId).isPresent());
        Order cancelledOrder = orderRepository.findById(orderId).get();
        assertTrue(cancelledOrder.isCancelled());
        assertEquals(10L, product.getInventory());
    }

    @Test
    @Transactional
    public void testCancelOrder() {
        Product product = getMockProduct("Product", 1.0, 10L);
        productRepository.save(product);
        Order order = getMockOrder(List.of(getMockOrderItem(product, 3L)));
        orderRepository.save(order);

        orderService.cancelOrder(order.getId());

        assertTrue(orderRepository.findById(order.getId()).isPresent());
        Order cancelledOrder = orderRepository.findById(order.getId()).get();
        assertTrue(cancelledOrder.isCancelled());
        assertEquals(13, product.getInventory());
    }

    @Test
    public void testPayOrder() {
        Product product = getMockProduct("Product", 1.0, 10L);
        productRepository.save(product);
        Order order = getMockOrder(List.of(getMockOrderItem(product, 3L)));
        orderRepository.save(order);

        orderService.payOrder(order.getId());

        assertTrue(orderRepository.findById(order.getId()).isPresent());
        Order paidOrder = orderRepository.findById(order.getId()).get();
        assertTrue(paidOrder.isPaid());
    }

    private Product getMockProduct(String name, Double price, Long inventory){
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setInventory(inventory);
        return product;
    }

    private Order getMockOrder(List<OrderItem> items){
        Order order = new Order();
        order.setItems(items);
        return order;
    }

    private OrderItem getMockOrderItem(Product product, Long quantity){
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setQuantity(quantity);
        return orderItem;
    }

}
