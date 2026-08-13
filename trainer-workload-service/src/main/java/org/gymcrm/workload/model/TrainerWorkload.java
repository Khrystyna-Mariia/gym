package org.gymcrm.workload.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trainer_workload")
@Getter
@Setter
@NoArgsConstructor
public class TrainerWorkload {

    @Id
    @Column(name = "trainer_username")
    private String trainerUsername;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @OneToMany(mappedBy = "trainerWorkload", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<YearlyWorkload> years = new ArrayList<>();
}