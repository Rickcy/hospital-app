package nl.gerimedica.assignment.service;

import java.util.Comparator;
import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.gerimedica.assignment.domain.entity.Appointment;
import nl.gerimedica.assignment.domain.entity.Patient;
import nl.gerimedica.assignment.domain.repository.AppointmentRepository;
import nl.gerimedica.assignment.web.dto.AppointmentDto;
import nl.gerimedica.assignment.web.mapper.AppointmentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * Service for CRUD and queries related to Appointment entity.
 */

@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class AppointmentService
{
    private final AppointmentRepository appointmentRepository;

    /**
     * Creates a single appointment for the given patient from DTO.
     */
    @Transactional
    public Appointment createForPatient(Patient patient, @Valid AppointmentDto dto)
    {
        return appointmentRepository.save(AppointmentMapper.toEntity(dto, patient));
    }

    /**
     * Creates multiple appointments for a patient in a single transaction.
     * Intended for bulk operations.
     */
    @Transactional
    public List<Appointment> createBatchForPatient(Patient patient, List<@Valid AppointmentDto> dtos)
    {
        var toSave = dtos.stream().peek(appt ->
        {
            log.info("Created appointment for reason: {} [Date: {}] [Patient SSN: {}]", appt.reason(), appt.date(),
                    patient.getSsn());
        }).map(a -> AppointmentMapper.toEntity(a, patient)).toList();
        return appointmentRepository.saveAll(toSave);
    }

    /**
     * Returns all appointments.
     */
    @Transactional(readOnly = true)
    public List<Appointment> listAll()
    {
        return appointmentRepository.findAll();
    }

    /**
     * Finds appointments by exact reason ignoring case (executed at DB level).
     */
    @Transactional(readOnly = true)
    public List<Appointment> findByReasonExactIgnoreCase(String reason)
    {
        return appointmentRepository.findByReasonIgnoreCase(reason);
    }
    /**
     * Returns the latest appointment by date for the given patient, or null if none.
     */
    @Transactional(readOnly = true)
    public Appointment findLatestFor(Patient patient)
    {
        if (patient.getAppointments() == null || patient.getAppointments().isEmpty()) return null;
        return patient.getAppointments().stream().max(Comparator.comparing(Appointment::getDate)).orElse(null);
    }
    /**
     * Deletes all appointments associated with the given patient.
     */
    @Transactional
    public void deleteAllFor(Patient patient)
    {
        var appts = List.copyOf(patient.getAppointments());
        appointmentRepository.deleteAll(appts);
    }

}
