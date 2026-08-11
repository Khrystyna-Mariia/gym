package org.gymcrm.workload.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "yearly_workload", uniqueConstraints = @UniqueConstraint(columnNames = {"trainer_username", "year"}))
@Getter
@Setter
@NoArgsConstructor
public class YearlyWorkload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_username", nullable = false)
    private TrainerWorkload trainerWorkload;

    @Column(name = "workload_year", nullable = false)
    private int year;

    @OneToMany(mappedBy = "yearlyWorkload", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<MonthlyWorkload> months = new ArrayList<>();
}