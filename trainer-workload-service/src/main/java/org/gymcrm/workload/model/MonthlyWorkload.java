package org.gymcrm.workload.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@NoArgsConstructor
public class MonthlyWorkload {

    @NotNull(message = "Month is required")
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    @Field("month")
    private Integer month;

    @NotNull(message = "Summary duration is required")
    @Min(value = 0, message = "Summary duration cannot be negative")
    @Field("summaryDuration")
    private Integer summaryDuration;
}