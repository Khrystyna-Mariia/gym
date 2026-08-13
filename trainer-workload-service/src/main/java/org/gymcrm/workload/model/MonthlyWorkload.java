package org.gymcrm.workload.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "monthly_workload", uniqueConstraints = @UniqueConstraint(columnNames = {"yearly_workload_id", "month"}))
@Getter
@Setter
@NoArgsConstructor
public class MonthlyWorkload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "yearly_workload_id", nullable = false)
    private YearlyWorkload yearlyWorkload;

    @Column(name = "workload_month", nullable = false)
    private int month;

    @Column(name = "summary_duration", nullable = false)
    private int summaryDuration;
}