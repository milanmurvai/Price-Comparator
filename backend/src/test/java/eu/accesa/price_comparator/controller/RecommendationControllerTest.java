package eu.accesa.price_comparator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.accesa.price_comparator.dto.product.SubstituteProductRequest;
import eu.accesa.price_comparator.dto.product.SubstituteProductResponse;
import eu.accesa.price_comparator.service.RecommendationService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RecommendationService recommendationService;

    @MockBean
    private JavaMailSender mailSender;

    @Test
    void testGetSubstitutes() throws Exception {
        SubstituteProductRequest request = new SubstituteProductRequest("lapte", LocalDate.of(2025, 5, 1));
        List<SubstituteProductResponse> mockResponse = List.of(
                new SubstituteProductResponse("Carrefour", "P001", "lapte", "Zuzu", "lactate",
                        5.99, 5.99, 1.0, "l")
        );

        when(recommendationService.getRecommendedSubstitutes(any())).thenReturn(mockResponse);

        mockMvc.perform(get("/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].store").value("Carrefour"))
                .andExpect(jsonPath("$[0].productId").value("P001"))
                .andExpect(jsonPath("$[0].name").value("lapte"))
                .andExpect(jsonPath("$[0].brand").value("Zuzu"))
                .andExpect(jsonPath("$[0].category").value("lactate"))
                .andExpect(jsonPath("$[0].pricePerUnit").value(5.99))
                .andExpect(jsonPath("$[0].totalPrice").value(5.99))
                .andExpect(jsonPath("$[0].quantity").value(1.0))
                .andExpect(jsonPath("$[0].unit").value("l"));
    }
}
