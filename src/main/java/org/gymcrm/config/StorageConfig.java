package org.gymcrm.config;

import org.gymcrm.model.Trainee;
import org.gymcrm.model.Trainer;
import org.gymcrm.model.Training;
import org.gymcrm.model.TrainingType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class StorageConfig {

    @Bean
    public Map<Long, Trainee> traineeStorage() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    public Map<Long, Trainer> trainerStorage() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    public Map<Long, Training> trainingStorage() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    public Map<Long, TrainingType> trainingTypeStorage() {
        return new ConcurrentHashMap<>();
    }

}