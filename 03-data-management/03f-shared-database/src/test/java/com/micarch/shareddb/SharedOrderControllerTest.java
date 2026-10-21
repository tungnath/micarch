package com.micarch.shareddb;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(SharedOrderController.class)
class SharedOrderControllerTest {
    @Autowired MockMvc mvc;

    @Test
    void sharedTableCanBeReadByAnotherContext() throws Exception {
        mvc.perform(post("/api/shared/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"O-1\",\"productId\":\"P-1\",\"quantity\":2,\"state\":\"PENDING\"}"));
        mvc.perform(get("/api/shared/orders/O-1"))
                .andExpect(jsonPath("$.state").value("PENDING"));
    }
}
