package nl.gerimedica.assignment.web.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nl.gerimedica.assignment.service.HospitalService;
import nl.gerimedica.assignment.web.dto.AppointmentDto;
import nl.gerimedica.assignment.web.dto.PatientDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/appointment")
@RequiredArgsConstructor
@Tag(name = "Appointments", description = "Operations for creating and querying appointments")
public class AppointmentController
{

    private final HospitalService hospitalService;


    /**
     * Creates multiple appointments for a patient identified by SSN.
     * If patient does not exist, it will be created.
     * Returns list of created appointments (reason, date).
     */
    @Operation(summary = "Create appointments in bulk",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PatientDto.class), examples = @ExampleObject(value = """
                            {
                              "name": "John Doe",
                              "ssn": "123-45-6789",
                              "appointments": [
                                { "reason": "Checkup", "date": "2025-02-01" },
                                { "reason": "Follow-up", "date": "2025-02-15" }
                              ]
                            }
                            """))), responses = {
            @ApiResponse(responseCode = "201", description = "Appointments created",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = AppointmentDto.class)))),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "409", description = "Data integrity violation"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @PostMapping("/batch")
    public ResponseEntity<List<AppointmentDto>> createBulk(@RequestBody @Valid PatientDto payload)
    {
        var created = hospitalService.bulkCreateAppointments(payload);
        return ResponseEntity.ok(created);
    }

    /**
     * Returns appointments that match the given reason exactly (case-insensitive).
     */
    @Operation(summary = "Get appointments by exact reason (case-insensitive)", responses = {
            @ApiResponse(responseCode = "200", description = "List of matching appointments",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = AppointmentDto.class))))})
    @GetMapping
    public ResponseEntity<List<AppointmentDto>> getByReason(@RequestParam(name = "reason") String reason)
    {
        var result = hospitalService.getAppointmentsByReason(reason);
        return ResponseEntity.ok(result);
    }

    /**
     * Deletes all appointments for a patient by SSN.
     * If patient not found, operation is a no-op and returns 204.
     */
    @Operation(summary = "Delete all appointments by patient's SSN",
            responses = {@ApiResponse(responseCode = "204", description = "Appointments deleted or nothing to delete")})
    @DeleteMapping("/by-ssn/{ssn}")
    public ResponseEntity<Void> deleteBySsn(@PathVariable String ssn)
    {
        hospitalService.deleteAppointmentsBySSN(ssn);
        return ResponseEntity.noContent().build();
    }

    /**
     * Returns the latest appointment (by date) for a patient SSN, or 404 if not found.
     */
    @Operation(summary = "Get latest appointment by patient's SSN", responses = {
            @ApiResponse(responseCode = "200", description = "Latest appointment",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AppointmentDto.class))),
            @ApiResponse(responseCode = "404", description = "Patient or appointment not found")})
    @GetMapping("/latest")
    public ResponseEntity<AppointmentDto> getLatest(@RequestParam String ssn)
    {
        var dto = hospitalService.findLatestAppointmentBySSN(ssn);
        if (dto == null)
        {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }
}