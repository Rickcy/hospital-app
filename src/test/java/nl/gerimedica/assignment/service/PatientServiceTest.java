package nl.gerimedica.assignment.service;

import java.util.List;
import java.util.Optional;

import nl.gerimedica.assignment.domain.entity.Patient;
import nl.gerimedica.assignment.domain.repository.PatientRepository;
import nl.gerimedica.assignment.web.dto.PatientDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for PatientService using Mockito.
 */
@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientService patientService;

    @Test
    void findAll_returnsRepositoryResult() {
        when(patientRepository.findAll()).thenReturn(List.of(new Patient("A", "1")));
        var result = patientService.findAll();
        verify(patientRepository).findAll();
        assertEquals(1, result.size());
        assertEquals("A", result.get(0).getName());
    }

    @Test
    void getById_returnsPatient() {
        var p = new Patient("John", "123");
        when(patientRepository.findById(10L)).thenReturn(Optional.of(p));

        var result = patientService.getById(10L);

        verify(patientRepository).findById(10L);
        assertSame(p, result);
    }

    @Test
    void getById_throwsWhenNotFound() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());
        var ex = assertThrows(IllegalArgumentException.class, () -> patientService.getById(99L));
        assertTrue(ex.getMessage().contains("Patient not found"));
    }

    @Test
    void getBySsnOrNull_returnsNullWhenAbsent() {
        when(patientRepository.findBySsn("missing")).thenReturn(Optional.empty());
        var result = patientService.getBySsnOrNull("missing");
        verify(patientRepository).findBySsn("missing");
        assertNull(result);
    }

    @Test
    void getBySsnOrNull_returnsPatient() {
        var p = new Patient("Jane", "321");
        when(patientRepository.findBySsn("321")).thenReturn(Optional.of(p));
        var result = patientService.getBySsnOrNull("321");
        assertSame(p, result);
    }

    @Test
    void getOrCreateBySsn_returnsExisting() {
        var existing = new Patient("X", "SSN1");
        var dto = new PatientDto("X", "SSN1", List.of());
        when(patientRepository.findBySsn("SSN1")).thenReturn(Optional.of(existing));

        var result = patientService.getOrCreateBySsn(dto);

        verify(patientRepository, never()).save(any());
        assertSame(existing, result);
    }

    @Test
    void getOrCreateBySsn_createsNewWhenNotFound() {
        var dto = new PatientDto("New", "SSN2", List.of());
        when(patientRepository.findBySsn("SSN2")).thenReturn(Optional.empty());
        when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));

        var created = patientService.getOrCreateBySsn(dto);

        ArgumentCaptor<Patient> captor = ArgumentCaptor.forClass(Patient.class);
        verify(patientRepository).save(captor.capture());
        Patient toSave = captor.getValue();
        assertEquals("New", toSave.getName());
        assertEquals("SSN2", toSave.getSsn());

        assertEquals("New", created.getName());
        assertEquals("SSN2", created.getSsn());
    }

    @Test
    void create_savesMappedEntity() {
        var dto = new PatientDto("P", "SSN3", List.of());
        when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));

        var saved = patientService.create(dto);

        ArgumentCaptor<Patient> captor = ArgumentCaptor.forClass(Patient.class);
        verify(patientRepository).save(captor.capture());
        Patient p = captor.getValue();
        assertEquals("P", p.getName());
        assertEquals("SSN3", p.getSsn());
        assertEquals("P", saved.getName());
        assertEquals("SSN3", saved.getSsn());
    }

    @Test
    void update_modifiesFieldsWhenPresent() {
        var existing = new Patient("Old", "OLD-SSN");
        when(patientRepository.findById(7L)).thenReturn(Optional.of(existing));

        var dto = new PatientDto("NewName", "NEW-SSN", List.of());
        var updated = patientService.update(7L, dto);

        assertEquals("NewName", existing.getName());
        assertEquals("NEW-SSN", existing.getSsn());
        assertSame(existing, updated);
    }

    @Test
    void update_keepsFieldsWhenDtoNulls() {
        var existing = new Patient("Keep", "KEEP-SSN");
        when(patientRepository.findById(8L)).thenReturn(Optional.of(existing));

        var dto = new PatientDto(null, null, List.of());
        var updated = patientService.update(8L, dto);

        assertEquals("Keep", updated.getName());
        assertEquals("KEEP-SSN", updated.getSsn());
    }
}