package nl.gerimedica.assignment.web.mapper;

import nl.gerimedica.assignment.domain.entity.Appointment;
import nl.gerimedica.assignment.domain.entity.Patient;
import nl.gerimedica.assignment.web.dto.AppointmentDto;

/**
 * Mapper for converting between Appointment entity and AppointmentDto.
 */
public final class AppointmentMapper
{

    private AppointmentMapper()
    {
    }
    /**
     * Creates Appointment entity from DTO and binds it to a patient.
     */
    public static Appointment toEntity(AppointmentDto dto, Patient patient)
    {
        if (dto == null) return null;
        return new Appointment(dto.reason(), dto.date(), patient);
    }
    /**
     * Creates DTO from Appointment entity.
     */
    public static AppointmentDto toDto(Appointment entity)
    {
        if (entity == null) return null;
        return new AppointmentDto(entity.getReason(), entity.getDate());
    }
}
