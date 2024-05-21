package cz.rohlik.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.rohlik.exception.ProductNotFoundException;
import cz.rohlik.model.ProductCreateRequest;
import cz.rohlik.model.ProductCreateResponse;
import cz.rohlik.model.ProductUpdateRequest;
import cz.rohlik.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ProductController.class)
@ActiveProfiles("it")
public class ProductControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCreateProduct_Success() throws Exception {
        ProductCreateRequest request = new ProductCreateRequest().name("Test Product").price(2.0).inventory(2L);
        Long expectedProductId = 1L;

        when(productService.createProduct(any(ProductCreateRequest.class))).thenReturn(expectedProductId);
        MvcResult mvcResult = mockMvc.perform(post("/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();
        ProductCreateResponse response = objectMapper.readValue(responseBody, ProductCreateResponse.class);
        assertEquals(expectedProductId, response.getId());
    }

    @Test
    public void testCreateProduct_BadRequest() throws Exception {
        ProductCreateRequest request = new ProductCreateRequest();

        mockMvc.perform(post("/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDeleteProductById_Success() throws Exception {
        Long productId = 1L;

        mockMvc.perform(delete("/product/" + productId))
                .andExpect(status().isOk());
    }

    @Test
    public void testDeleteProductById_NotFound() throws Exception {
        Long productId = 1L;
        doThrow(new ProductNotFoundException(productId)).when(productService).deleteProductById(productId);

        mockMvc.perform(delete("/product/" + productId))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateProduct_Success() throws Exception {
        Long productId = 1L;
        ProductUpdateRequest updateRequest = new ProductUpdateRequest().name("Updated Name");

        mockMvc.perform(patch("/product/" + productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
    }

    @Test
    public void testUpdateProduct_NotFound() throws Exception {
        Long productId = 1L;
        ProductUpdateRequest updateRequest = new ProductUpdateRequest().name("Updated Name");

        doThrow(new ProductNotFoundException(productId)).when(productService).updateProduct(productId, updateRequest);

        mockMvc.perform(patch("/product/" + productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateProduct_BadRequest() throws Exception {
        Long productId = 1L;

        mockMvc.perform(patch("/product/" + productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }
}
