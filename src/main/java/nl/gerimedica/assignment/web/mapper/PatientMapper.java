package nl.gerimedica.assignment.web.mapper;

import java.util.List;
import java.util.Objects;

import nl.gerimedica.assignment.domain.entity.Appointment;
import nl.gerimedica.assignment.domain.entity.Patient;
import nl.gerimedica.assignment.web.dto.AppointmentDto;
import nl.gerimedica.assignment.web.dto.PatientDto;

/**
 * Mapper for converting between Patient entity and PatientDto.
 */
public final class PatientMapper
{

    private PatientMapper()
    {
    }

    /**
     * Creates Patient entity from DTO (without appointments).
     */
    public static Patient toEntity(PatientDto dto)
    {
        if (dto == null) return null;
        return new Patient(dto.name(), dto.ssn());
    }

    /**
     * Creates DTO from Patient entity, mapping appointments into AppointmentDto list.
     */
    public static PatientDto toDto(Patient entity)
    {
        if (entity == null) return null;
        List<AppointmentDto> appts = entity.getAppointments() == null ? List.of() :
                entity.getAppointments().stream().filter(Objects::nonNull)
                        .map(a -> new AppointmentDto(a.getReason(), a.getDate())).toList();
        return new PatientDto(entity.getName(), entity.getSsn(), appts);
    }
    /**
     * Converts list of AppointmentDto to Appointment entities bound to the given patient.
     */
    public static List<Appointment> appointmentsToEntities(Patient patient, List<AppointmentDto> dtos)
    {
        if (dtos == null) return List.of();
        return dtos.stream().filter(Objects::nonNull).map(d -> new Appointment(d.reason(), d.date(), patient)).toList();
    }
}