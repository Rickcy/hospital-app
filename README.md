config.CacheConfig
Fixed: Introduced a dedicated config instead of implicit/no caching.

config.OpenApiConfig
Fixed: Added structured API docs where there were none.

domain.entity.Patient
Fixed:
Replaced public fields with encapsulated getters/setters (immutability of id).
Added @NotBlank/@Size validations.
Enforced unique SSN with a unique constraint and index.
Proper equals/hashCode based on id to avoid collisions and proxy issues.
Configured one-to-many with cascade = ALL and orphanRemoval, LAZY loading.
Added @JsonManagedReference to avoid JSON cycles.

domain.entity.Appointment
Fixed:
Switched date to LocalDate (type-safe vs String).
Added @NotBlank/@NotNull and column constraints.
Added DB index for reason to speed queries.
Proper equals/hashCode by id.
LAZY many-to-one with explicit foreign key naming.
Added @JsonBackReference to prevent serialization recursion.

domain.repository.PatientRepository
Fixed:
Removed manual in-memory scanning.
Embraced Optional for null-safety.
Added @EntityGraph to mitigate N+1 where appropriate.

domain.repository.AppointmentRepository
Fixed:
Moved reason filtering to the database (exact, case-insensitive).
Added @EntityGraph on findAll to load patient when needed, reducing N+1.

exception.GlobalExceptionHandler
Fixed:
Unified error format with timestamp/status/message.
Mapped common exceptions to 400/404/409/500 accordingly.

service.PatientService
Fixed:
Introduced @Transactional boundaries.
Added @Cacheable/@CacheEvict for SSN lookups and updates.
Replaced manual loops with repository queries.
Validated DTO inputs and used mapping instead of exposing entities in controllers.

service.AppointmentService
Centralized business rules (no logic in controllers).
Batch operations use saveAll; deletion uses a safe snapshot copy.
DB-driven queries (findByReasonIgnoreCase).
Comparator for latest appointment by LocalDate

HospitalService
Fixed:
Returns DTOs instead of entities.
Uses PatientService.getOrCreateBySsn to avoid duplicates/races.
Records usage via HospitalUtils.

util.HospitalUtils
Fixed:
Replaced non-thread-safe static int with AtomicLong.
Made class non-instantiable and purely static.

web.controller.AppointmentController
Fixed:
Introduced DTO-based input/output.
Proper status codes (200/204/404).
Removed ad-hoc request formats in favor of structured JSON payload.

web.dto.PatientDto
web.dto.AppointmentDto
web.mapper.PatientMapper
web.mapper.AppointmentMapper