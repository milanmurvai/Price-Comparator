package eu.accesa.price_comparator.controller;

import eu.accesa.price_comparator.service.CsvDispatcherService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CsvImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CsvDispatcherService csvDispatcherService;

    @MockBean
    private JavaMailSender mailSender;

    @Test
    @DisplayName("Successful CSV import returns 200")
    void testImportCsv_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", "content".getBytes());

        doNothing().when(csvDispatcherService).importCsv(file);

        mockMvc.perform(multipart("/import/csv").file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("Imported: test.csv"));
    }

    @Test
    @DisplayName("Invalid CSV triggers 400 Bad Request")
    void testImportCsv_BadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "bad.csv", "text/csv", new byte[0]);

        doThrow(new IllegalArgumentException("Invalid CSV format")).when(csvDispatcherService).importCsv(file);

        mockMvc.perform(multipart("/import/csv").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid CSV format"));
    }

    @Test
    @DisplayName("Unexpected error triggers 500 Internal Server Error")
    void testImportCsv_ServerError() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "error.csv", "text/csv", new byte[0]);

        doThrow(new RuntimeException("Disk failure")).when(csvDispatcherService).importCsv(file);

        mockMvc.perform(multipart("/import/csv").file(file))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Server error: Disk failure"));
    }
}
