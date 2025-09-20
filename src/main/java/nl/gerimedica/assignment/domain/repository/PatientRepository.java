package nl.gerimedica.assignment.domain.repository;

import java.util.Optional;

import nl.gerimedica.assignment.domain.entity.Patient;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long>
{
    Optional<Patient> findBySsn(String ssn);

    @EntityGraph(attributePaths = "appointments")
    Optional<Patient> findWithAppointmentsById(Long id);

}
