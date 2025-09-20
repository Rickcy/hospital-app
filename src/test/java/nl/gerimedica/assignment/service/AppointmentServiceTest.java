package nl.gerimedica.assignment.service;

import nl.gerimedica.assignment.domain.entity.Appointment;
import nl.gerimedica.assignment.domain.entity.Patient;
import nl.gerimedica.assignment.domain.repository.AppointmentRepository;
import nl.gerimedica.assignment.web.dto.AppointmentDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AppointmentService using Mockito.
 */
@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    @Test
    void createForPatient_savesAndReturnsAppointment() {
        // Arrange
        Patient patient = new Patient("John Doe", "123-45-6789");
        AppointmentDto dto = new AppointmentDto("Checkup", LocalDate.of(2025, 2, 1));
        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Appointment saved = appointmentService.createForPatient(patient, dto);

        // Assert
        verify(appointmentRepository).save(captor.capture());
        Appointment toSave = captor.getValue();
        assertEquals("Checkup", toSave.getReason());
        assertEquals(LocalDate.of(2025, 2, 1), toSave.getDate());
        assertSame(patient, toSave.getPatient());

        assertEquals("Checkup", saved.getReason());
        assertEquals(LocalDate.of(2025, 2, 1), saved.getDate());
        assertSame(patient, saved.getPatient());
    }

    @Test
    void createBatchForPatient_savesAllAndReturnsList() {
        // Arrange
        Patient patient = new Patient("Jane Roe", "987-65-4321");
        List<AppointmentDto> dtos = List.of(
                new AppointmentDto("X-Ray", LocalDate.of(2025, 3, 1)),
                new AppointmentDto("Follow-up", LocalDate.of(2025, 3, 15))
        );
        when(appointmentRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        List<Appointment> saved = appointmentService.createBatchForPatient(patient, dtos);

        // Assert
        verify(appointmentRepository).saveAll(anyList());
        assertEquals(2, saved.size());
        assertEquals("X-Ray", saved.get(0).getReason());
        assertEquals(LocalDate.of(2025, 3, 1), saved.get(0).getDate());
        assertSame(patient, saved.get(0).getPatient());
        assertEquals("Follow-up", saved.get(1).getReason());
        assertEquals(LocalDate.of(2025, 3, 15), saved.get(1).getDate());
        assertSame(patient, saved.get(1).getPatient());
    }

    @Test
    void listAll_delegatesToRepository() {
        // Arrange
        when(appointmentRepository.findAll()).thenReturn(List.of());

        // Act
        List<Appointment> all = appointmentService.listAll();

        // Assert
        verify(appointmentRepository).findAll();
        assertNotNull(all);
    }

    @Test
    void findByReasonExactIgnoreCase_delegatesToRepository() {
        // Arrange
        when(appointmentRepository.findByReasonIgnoreCase("Checkup")).thenReturn(List.of());

        // Act
        List<Appointment> result = appointmentService.findByReasonExactIgnoreCase("Checkup");

        // Assert
        verify(appointmentRepository).findByReasonIgnoreCase("Checkup");
        assertNotNull(result);
    }

    @Test
    void findLatestFor_returnsNullWhenNoAppointments() {
        // Arrange
        Patient patient = new Patient("Empty", "000-00-0000");

        // Act
        var latest = appointmentService.findLatestFor(patient);

        // Assert
        assertNull(latest);
    }

    @Test
    void findLatestFor_returnsMaxByDate() {
        // Arrange
        Patient patient = new Patient("With Appts", "111-11-1111");
        Appointment a1 = new Appointment("A1", LocalDate.of(2025, 1, 1), patient);
        Appointment a2 = new Appointment("A2", LocalDate.of(2025, 5, 10), patient);
        Appointment a3 = new Appointment("A3", LocalDate.of(2025, 3, 20), patient);
        patient.getAppointments().addAll(List.of(a1, a2, a3));

        // Act
        var latest = appointmentService.findLatestFor(patient);

        // Assert
        assertNotNull(latest);
        assertEquals("A2", latest.getReason());
        assertEquals(LocalDate.of(2025, 5, 10), latest.getDate());
    }

    @Test
    void deleteAllFor_deletesCopiedList() {
        // Arrange
        Patient patient = new Patient("To Delete", "222-22-2222");
        Appointment a1 = new Appointment("A1", LocalDate.of(2025, 1, 1), patient);
        Appointment a2 = new Appointment("A2", LocalDate.of(2025, 2, 2), patient);
        patient.getAppointments().addAll(List.of(a1, a2));

        // Act
        appointmentService.deleteAllFor(patient);

        // Assert
        ArgumentCaptor<List<Appointment>> captor = ArgumentCaptor.forClass(List.class);
        verify(appointmentRepository).deleteAll(captor.capture());
        List<Appointment> passed = captor.getValue();
        assertEquals(2, passed.size());
        assertTrue(passed.containsAll(List.of(a1, a2)));
    }
}