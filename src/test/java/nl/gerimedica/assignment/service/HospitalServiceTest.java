package nl.gerimedica.assignment.service;

import nl.gerimedica.assignment.domain.entity.Appointment;
import nl.gerimedica.assignment.domain.entity.Patient;
import nl.gerimedica.assignment.web.dto.AppointmentDto;
import nl.gerimedica.assignment.web.dto.PatientDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for HospitalService.
 */
@ExtendWith(MockitoExtension.class)
class HospitalServiceTest {

    @Mock
    private PatientService patientService;

    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private HospitalService hospitalService;

    @Test
    void bulkCreateAppointments_createsPatientIfNeededAndReturnsDtos() {
        // Arrange
        PatientDto payload = new PatientDto("John Doe", "123-45-6789",
                List.of(new AppointmentDto("Checkup", LocalDate.of(2025, 2, 1)),
                        new AppointmentDto("Follow-up", LocalDate.of(2025, 2, 15))));
        Patient patient = new Patient("John Doe", "123-45-6789");

        Appointment a1 = new Appointment("Checkup", LocalDate.of(2025, 2, 1), patient);
        Appointment a2 = new Appointment("Follow-up", LocalDate.of(2025, 2, 15), patient);

        when(patientService.getOrCreateBySsn(payload)).thenReturn(patient);
        when(appointmentService.createBatchForPatient(patient, payload.appointments()))
                .thenReturn(List.of(a1, a2));

        // Act
        List<AppointmentDto> result = hospitalService.bulkCreateAppointments(payload);

        // Assert
        verify(patientService).getOrCreateBySsn(payload);
        verify(appointmentService).createBatchForPatient(patient, payload.appointments());
        assertEquals(2, result.size());
        assertEquals("Checkup", result.get(0).reason());
        assertEquals(LocalDate.of(2025, 2, 1), result.get(0).date());
        assertEquals("Follow-up", result.get(1).reason());
        assertEquals(LocalDate.of(2025, 2, 15), result.get(1).date());
    }

    @Test
    void getAppointmentsByReason_returnsDtosFromService() {
        // Arrange
        String reason = "X-Ray";
        Patient p = new Patient("Jane", "111-11-1111");
        Appointment a = new Appointment("X-Ray", LocalDate.of(2025, 3, 1), p);
        when(appointmentService.findByReasonExactIgnoreCase(reason)).thenReturn(List.of(a));

        // Act
        List<AppointmentDto> result = hospitalService.getAppointmentsByReason(reason);

        // Assert
        verify(appointmentService).findByReasonExactIgnoreCase(reason);
        assertEquals(1, result.size());
        assertEquals("X-Ray", result.get(0).reason());
        assertEquals(LocalDate.of(2025, 3, 1), result.get(0).date());
    }

    @Test
    void deleteAppointmentsBySSN_noOpWhenPatientNotFound() {
        // Arrange
        when(patientService.getBySsnOrNull("missing")).thenReturn(null);

        // Act
        hospitalService.deleteAppointmentsBySSN("missing");

        // Assert
        verify(appointmentService, never()).deleteAllFor(any());
    }

    @Test
    void deleteAppointmentsBySSN_deletesWhenPatientFound() {
        // Arrange
        Patient p = new Patient("J", "222-22-2222");
        when(patientService.getBySsnOrNull("222-22-2222")).thenReturn(p);

        // Act
        hospitalService.deleteAppointmentsBySSN("222-22-2222");

        // Assert
        verify(appointmentService).deleteAllFor(p);
    }

    @Test
    void findLatestAppointmentBySSN_returnsNullWhenPatientMissing() {
        // Arrange
        when(patientService.getBySsnOrNull("none")).thenReturn(null);

        // Act
        var result = hospitalService.findLatestAppointmentBySSN("none");

        // Assert
        assertNull(result);
        verify(appointmentService, never()).findLatestFor(any());
    }

    @Test
    void findLatestAppointmentBySSN_mapsLatestToDto() {
        // Arrange
        Patient p = new Patient("Amy", "333-33-3333");
        Appointment latest = new Appointment("Latest", LocalDate.of(2025, 6, 30), p);
        when(patientService.getBySsnOrNull("333-33-3333")).thenReturn(p);
        when(appointmentService.findLatestFor(p)).thenReturn(latest);

        // Act
        var dto = hospitalService.findLatestAppointmentBySSN("333-33-3333");

        // Assert
        assertNotNull(dto);
        assertEquals("Latest", dto.reason());
        assertEquals(LocalDate.of(2025, 6, 30), dto.date());
    }
}