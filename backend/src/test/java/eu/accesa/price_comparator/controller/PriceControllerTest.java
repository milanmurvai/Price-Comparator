package eu.accesa.price_comparator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.accesa.price_comparator.dto.price.PriceAlertRequest;
import eu.accesa.price_comparator.dto.price.PriceHistoryPoint;
import eu.accesa.price_comparator.dto.price.PriceHistoryRequest;
import eu.accesa.price_comparator.dto.price.PriceHistoryResponse;
import eu.accesa.price_comparator.service.PriceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PriceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PriceService priceService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JavaMailSender mailSender;

    @Test
    void testGetPriceHistory() throws Exception {
        PriceHistoryRequest request = new PriceHistoryRequest("lapte", LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 10), null, null, null);
        List<PriceHistoryResponse> mockResponse = List.of(
                new PriceHistoryResponse("Carrefour", "Zuzu", "lactate",
                        List.of(new PriceHistoryPoint(LocalDate.of(2025, 5, 1), 5.99)))
        );

        when(priceService.getPriceHistory(any())).thenReturn(mockResponse);

        mockMvc.perform(post("/prices/history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].store").value("Carrefour"))
                .andExpect(jsonPath("$[0].brand").value("Zuzu"))
                .andExpect(jsonPath("$[0].category").value("lactate"))
                .andExpect(jsonPath("$[0].points[0].price").value(5.99));
    }

    @Test
    void testCreateAlert() throws Exception {
        PriceAlertRequest alertRequest = new PriceAlertRequest("test@example.com", "lapte", 4.99);

        when(priceService.createAlert(alertRequest)).thenReturn("Alert created successfully for product: lapte");

        mockMvc.perform(post("/prices/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(alertRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Alert created successfully for product: lapte"));
    }
}
