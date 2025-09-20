package nl.gerimedica.assignment.web.controller;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.gerimedica.assignment.service.HospitalService;
import nl.gerimedica.assignment.web.dto.AppointmentDto;
import nl.gerimedica.assignment.web.dto.PatientDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = AppointmentController.class)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HospitalService hospitalService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createBulk_returnsOkWithCreatedAppointments() throws Exception {
        // Arrange
        var payload = new PatientDto(
                "John Doe",
                "123-45-6789",
                List.of(
                        new AppointmentDto("Checkup", LocalDate.of(2025, 2, 1)),
                        new AppointmentDto("Follow-up", LocalDate.of(2025, 2, 15))
                )
        );
        var expected = List.of(
                new AppointmentDto("Checkup", LocalDate.of(2025, 2, 1)),
                new AppointmentDto("Follow-up", LocalDate.of(2025, 2, 15))
        );
        Mockito.when(hospitalService.bulkCreateAppointments(any(PatientDto.class))).thenReturn(expected);

        // Act & Assert
        mockMvc.perform(post("/api/v1/appointment/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].reason").value("Checkup"))
                .andExpect(jsonPath("$[0].date").value("2025-02-01"))
                .andExpect(jsonPath("$[1].reason").value("Follow-up"))
                .andExpect(jsonPath("$[1].date").value("2025-02-15"));
    }

    @Test
    void getByReason_returnsList() throws Exception {
        // Arrange
        var reason = "X-Ray";
        var list = List.of(new AppointmentDto("X-Ray", LocalDate.of(2025, 3, 1)));
        Mockito.when(hospitalService.getAppointmentsByReason(eq(reason))).thenReturn(list);

        // Act & Assert
        mockMvc.perform(get("/api/v1/appointment").param("reason", reason))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reason").value("X-Ray"))
                .andExpect(jsonPath("$[0].date").value("2025-03-01"));
    }

    @Test
    void deleteBySsn_returnsNoContent() throws Exception {
        // Arrange
        var ssn = "222-22-2222";
        Mockito.doNothing().when(hospitalService).deleteAppointmentsBySSN(ssn);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/appointment/by-ssn/{ssn}", ssn))
                .andExpect(status().isNoContent());
    }

    @Test
    void getLatest_returnsOkWhenExists() throws Exception {
        // Arrange
        var ssn = "333-33-3333";
        var dto = new AppointmentDto("Latest", LocalDate.of(2025, 6, 30));
        Mockito.when(hospitalService.findLatestAppointmentBySSN(ssn)).thenReturn(dto);

        // Act & Assert
        mockMvc.perform(get("/api/v1/appointment/latest").param("ssn", ssn))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reason").value("Latest"))
                .andExpect(jsonPath("$.date").value("2025-06-30"));
    }

    @Test
    void getLatest_returnsNotFoundWhenNull() throws Exception {
        // Arrange
        var ssn = "not-found";
        Mockito.when(hospitalService.findLatestAppointmentBySSN(ssn)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/v1/appointment/latest").param("ssn", ssn))
                .andExpect(status().isNotFound());
    }
}