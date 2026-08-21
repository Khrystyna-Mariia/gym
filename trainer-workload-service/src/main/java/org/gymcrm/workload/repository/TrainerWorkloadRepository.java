package org.gymcrm.workload.repository;

import org.gymcrm.workload.model.TrainerWorkload;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface TrainerWorkloadRepository extends MongoRepository<TrainerWorkload, String> {

    Optional<TrainerWorkload> findByTrainerUsername(String trainerUsername);

    List<TrainerWorkload> findByFirstNameAndLastName(String firstName, String lastName);
}