package nl.gerimedica.assignment.service;

import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.gerimedica.assignment.domain.entity.Appointment;
import nl.gerimedica.assignment.domain.entity.Patient;
import nl.gerimedica.assignment.util.HospitalUtils;
import nl.gerimedica.assignment.web.dto.AppointmentDto;
import nl.gerimedica.assignment.web.dto.PatientDto;
import nl.gerimedica.assignment.web.mapper.AppointmentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/**
 * Facade service orchestrating operations across patient and appointment services.
 * Keeps controller thin and coordinates multi-step flows.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class HospitalService
{

    private final PatientService patientService;
    private final AppointmentService appointmentService;

    /**
     * Ensures a patient exists by SSN and creates a batch of appointments for them.
     * Returns created appointments as DTOs.
     */
    @Transactional
    public List<AppointmentDto> bulkCreateAppointments(@Valid PatientDto payload)
    {
        Patient patient = patientService.getOrCreateBySsn(payload);

        List<Appointment> saved = appointmentService.createBatchForPatient(patient, payload.appointments());

        HospitalUtils.recordUsage("Bulk create appointments");

        return saved.stream().map(AppointmentMapper::toDto).toList();
    }

    /**
     * Finds appointments that match the reason exactly (case-insensitive).
     */
    @Transactional(readOnly = true)
    public List<AppointmentDto> getAppointmentsByReason(String reasonKeyword)
    {
        return appointmentService.findByReasonExactIgnoreCase(reasonKeyword).stream().map(AppointmentMapper::toDto)
                .toList();
    }
    /**
     * Deletes all appointments for a patient found by SSN.
     * If patient is absent, does nothing.
     */
    @Transactional
    public void deleteAppointmentsBySSN(String ssn)
    {
        Patient patient = patientService.getBySsnOrNull(ssn);
        if (patient == null) return;
        appointmentService.deleteAllFor(patient);
        HospitalUtils.recordUsage("Delete appointments by SSN");
    }
    /**
     * Returns the latest appointment for a patient SSN, or null if patient/appointments not found.
     */
    @Transactional(readOnly = true)
    public AppointmentDto findLatestAppointmentBySSN(String ssn)
    {
        Patient patient = patientService.getBySsnOrNull(ssn);
        if (patient == null) return null;
        var latest = appointmentService.findLatestFor(patient);
        return AppointmentMapper.toDto(latest);
    }
}
