package nl.gerimedica.assignment.service;

import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.gerimedica.assignment.domain.entity.Patient;
import nl.gerimedica.assignment.domain.repository.PatientRepository;
import nl.gerimedica.assignment.web.dto.PatientDto;
import nl.gerimedica.assignment.web.mapper.PatientMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * Service for CRUD and lookup operations on Patient entity.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class PatientService
{
    private final PatientRepository patientRepository;

    /**
     * Returns all patients.
     */
    @Transactional(readOnly = true)
    public List<Patient> findAll()
    {
        return patientRepository.findAll();
    }

    /**
     * Gets a patient by id or throws if not found.
     */
    @Transactional(readOnly = true)
    public Patient getById(Long id)
    {
        return patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + id));
    }

    /**
     * Returns a patient by SSN or null if absent. Result is cached.
     */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "patientBySsn", key = "#ssn")
    public Patient getBySsnOrNull(String ssn)
    {
        return patientRepository.findBySsn(ssn).orElse(null);
    }

    /**
     * Returns existing patient by SSN or creates a new one atomically.
     * Handles race condition via DataIntegrityViolationException + re-read.
     */
    @Transactional
    @CacheEvict(cacheNames = "patientBySsn", key = "#dto.ssn()", beforeInvocation = false)
    public Patient getOrCreateBySsn(@Valid PatientDto dto)
    {
        Optional<Patient> found = patientRepository.findBySsn(dto.ssn());
        if (found.isPresent())
        {
            log.info("Existing patient found, SSN: {}", found.get().getSsn());
            return found.get();
        }
        else
        {
            log.info("Creating new patient with SSN: {}", dto.ssn());
            return patientRepository.save(PatientMapper.toEntity(dto));
        }
    }

    /**
     * Creates a new patient from DTO.
     */
    @Transactional
    @CacheEvict(cacheNames = "patientBySsn", key = "#dto.ssn()", beforeInvocation = false)
    public Patient create(@Valid PatientDto dto)
    {
        return patientRepository.save(PatientMapper.toEntity(dto));
    }

    /**
     * Updates patient basic fields (name/ssn) if present in DTO.
     */
    @Transactional
    @CacheEvict(cacheNames = "patientBySsn", key = "#p0.ssn()", beforeInvocation = false, condition = "#result != null")
    public Patient update(Long id, @Valid PatientDto dto)
    {
        var p = getById(id);
        if (dto.name() != null) p.setName(dto.name());
        if (dto.ssn() != null) p.setSsn(dto.ssn());
        return p;
    }
}
