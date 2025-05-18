package eu.accesa.price_comparator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.accesa.price_comparator.dto.basket.BasketItem;
import eu.accesa.price_comparator.dto.basket.BasketRequest;
import eu.accesa.price_comparator.dto.basket.BasketResponse;
import eu.accesa.price_comparator.service.BasketService;
import org.junit.jupiter.api.DisplayName;
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

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BasketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BasketService basketService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JavaMailSender mailSender;

    @Test
    @DisplayName("POST /basket/optimize returns optimized basket")
    void testOptimizeBasket() throws Exception {
        BasketRequest request = new BasketRequest(List.of("P001", "P002"), LocalDate.of(2025, 5, 1));

        List<BasketResponse> response = List.of(
                new BasketResponse("Carrefour", List.of(
                        new BasketItem("P001", 5.99),
                        new BasketItem("P002", 2.99)
                ), 8.98)
        );

        when(basketService.optimizeBasket(request)).thenReturn(response);

        mockMvc.perform(post("/basket/optimize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].store").value("Carrefour"))
                .andExpect(jsonPath("$[0].items", hasSize(2)))
                .andExpect(jsonPath("$[0].total").value(8.98));
    }
}
