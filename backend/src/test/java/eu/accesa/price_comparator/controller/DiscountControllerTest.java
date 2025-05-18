package eu.accesa.price_comparator.controller;

import eu.accesa.price_comparator.dto.discount.BestDiscountDto;
import eu.accesa.price_comparator.service.DiscountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DiscountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DiscountService discountService;

    @MockBean
    private JavaMailSender mailSender;

    @Test
    void testGetBestDiscounts_WithDateParam() throws Exception {
        BestDiscountDto dto = new BestDiscountDto(
                "P001", "Carrefour", "Lapte", "Lactate", "Zuzu",
                10, LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 7)
        );

        when(discountService.getBestDiscounts(LocalDate.of(2025, 5, 1)))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/discounts/best")
                        .param("date", "2025-05-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId", is("P001")))
                .andExpect(jsonPath("$[0].store", is("Carrefour")))
                .andExpect(jsonPath("$[0].discount", is(10)));
    }

    @Test
    void testGetBestDiscounts_WithoutDateParam() throws Exception {
        BestDiscountDto dto = new BestDiscountDto(
                "P002", "Lidl", "Paine", "Panificatie", "VelPitar",
                20, LocalDate.now(), LocalDate.now().plusDays(3)
        );

        when(discountService.getBestDiscounts(LocalDate.now()))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/discounts/best"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId", is("P002")))
                .andExpect(jsonPath("$[0].store", is("Lidl")))
                .andExpect(jsonPath("$[0].discount", is(20)));
    }

    @Test
    void testGetTodayDiscounts() throws Exception {
        BestDiscountDto dto = new BestDiscountDto(
                "P003", "Mega", "Branza", "Lactate", "Hochland",
                15, LocalDate.now(), LocalDate.now().plusDays(2)
        );

        when(discountService.getTodayDiscounts())
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/discounts/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId", is("P003")))
                .andExpect(jsonPath("$[0].store", is("Mega")))
                .andExpect(jsonPath("$[0].discount", is(15)));
    }
}
