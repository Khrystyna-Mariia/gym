package org.gymcrm.workload.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "trainer_workload")
@CompoundIndex(name = "first_last_name_idx", def = "{'firstName': 1, 'lastName': 1}")
@Getter
@Setter
@NoArgsConstructor
public class TrainerWorkload {

    @Id
    private String trainerUsername;

    @NotBlank(message = "Trainer first name is required")
    @Field("firstName")
    private String firstName;

    @NotBlank(message = "Trainer last name is required")
    @Field("lastName")
    private String lastName;

    @NotNull(message = "Trainer status is required")
    @Field("active")
    private Boolean active;


    @Field("years")
    private List<YearlyWorkload> years = new ArrayList<>();
}