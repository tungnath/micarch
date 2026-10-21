package com.micarch.bff;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.micarch.bff.BffApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = BffApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("demo")
class BffControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void mobileDashboardUsesCompactClientShape() throws Exception {
        mockMvc.perform(get("/api/mobile/dashboard/C1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("C1"))
                .andExpect(jsonPath("$.recentOrders.length()").value(3))
                .andExpect(jsonPath("$.lowStockSkus.length()").value(2));
    }

    @Test
    void webDashboardUsesRichClientShape() throws Exception {
        mockMvc.perform(get("/api/web/dashboard/C1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("c1@example.test"))
                .andExpect(jsonPath("$.orders.length()").value(3))
                .andExpect(jsonPath("$.lowStockItems[0].productName").value("Laptop Stand"));
    }
}
