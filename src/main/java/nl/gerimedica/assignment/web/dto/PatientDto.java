package nl.gerimedica.assignment.web.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PatientDto(String name, @NotBlank @Size(max = 64) String ssn, List<AppointmentDto> appointments)
{
}
