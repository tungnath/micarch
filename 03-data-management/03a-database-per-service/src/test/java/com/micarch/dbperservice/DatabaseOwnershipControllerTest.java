package com.micarch.dbperservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(DatabaseOwnershipController.class)
class DatabaseOwnershipControllerTest {
    @Autowired MockMvc mvc;

    @Test
    void orderAndProductContextsHaveIndependentEndpoints() throws Exception {
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"O-1\",\"customerId\":\"C-1\",\"productId\":\"P-1\",\"quantity\":2}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"P-1\",\"name\":\"Book\",\"stock\":10}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/orders/O-1")).andExpect(jsonPath("$.customerId").value("C-1"));
        mvc.perform(get("/api/products/P-1")).andExpect(jsonPath("$.stock").value(10));
    }
}
