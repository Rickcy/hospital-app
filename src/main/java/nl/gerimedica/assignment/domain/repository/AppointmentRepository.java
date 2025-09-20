package nl.gerimedica.assignment.domain.repository;

import java.util.List;

import nl.gerimedica.assignment.domain.entity.Appointment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long>
{

    @EntityGraph(attributePaths = "patient")
    List<Appointment> findAll();

    List<Appointment> findByReasonIgnoreCase(String reason);

}
