package cz.rohlik.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.rohlik.model.OrderCreateRequest;
import cz.rohlik.model.OrderCreateResponse;
import cz.rohlik.model.OrderItemRequest;
import cz.rohlik.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@ActiveProfiles("it")
public class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCreateOrder_success() throws Exception {
        OrderCreateRequest request = new OrderCreateRequest().orderItems(List.of(
                new OrderItemRequest().productId(1L).quantity(3L)
        ));
        Long expectedOrderId = 1L;

        when(orderService.createOrder(request)).thenReturn(expectedOrderId);
        MvcResult mvcResult = mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();
        OrderCreateResponse response = objectMapper.readValue(responseBody, OrderCreateResponse.class);
        assertEquals(expectedOrderId, response.getId());
    }

    @Test
    public void testCreateOrder_badRequest() throws Exception {
        OrderCreateRequest request = new OrderCreateRequest().orderItems(List.of(new OrderItemRequest()));

        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();
    }


    @Test
    public void testCancelOrder_success() throws Exception {
        Long orderId = 1L;

        mockMvc.perform(post("/order/{id}/cancel", orderId))
                .andExpect(status().isOk())
                .andReturn();

        verify(orderService).cancelOrder(orderId);
    }


    @Test
    public void testPayOrder_success() throws Exception {
        Long orderId = 1L;

        mockMvc.perform(post("/order/{id}/pay", orderId))
                .andExpect(status().isOk())
                .andReturn();

        verify(orderService).payOrder(orderId);
    }

}
