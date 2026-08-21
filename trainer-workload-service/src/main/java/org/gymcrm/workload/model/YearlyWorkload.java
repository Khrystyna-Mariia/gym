package org.gymcrm.workload.model;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class YearlyWorkload {

    @NotNull(message = "Year is required")
    @Field("year")
    private Integer year;

    @Field("months")
    private List<MonthlyWorkload> months = new ArrayList<>();
}