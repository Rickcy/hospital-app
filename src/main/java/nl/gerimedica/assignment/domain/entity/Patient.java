package nl.gerimedica.assignment.domain.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "patients",
        uniqueConstraints = {@UniqueConstraint(name = "unique_idx_patients_ssn", columnNames = {"ssn"})},
        indexes = {@Index(name = "idx_patients_ssn", columnList = "ssn")})
public class Patient
{

    @Id
    @Getter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Getter
    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String name;

    @Setter
    @Getter
    @NotBlank
    @Size(max = 64)
    @Column(nullable = false, unique = true, length = 64)
    private String ssn;

    @Setter
    @Getter
    @OneToMany(mappedBy = "patient", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Appointment> appointments = new ArrayList<>();

    protected Patient()
    {
    }

    public Patient(String name, String ssn)
    {
        this.name = name;
        this.ssn = ssn;
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Patient patient)) return false;
        return Objects.equals(id, patient.id);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id);
    }
}
