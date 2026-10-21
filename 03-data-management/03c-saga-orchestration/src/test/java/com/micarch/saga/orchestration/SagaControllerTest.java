package com.micarch.saga.orchestration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SagaController.class)
class SagaControllerTest {
    @Autowired MockMvc mvc;

    @Test
    void paymentFailureProducesCompensation() throws Exception {
        mvc.perform(post("/api/sagas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":\"O-1\",\"quantity\":1,\"paymentAmount\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("COMPENSATED"))
                .andExpect(jsonPath("$.nextStep").value("release-stock"));
    }
}
