package org.gymcrm.storage;

import jakarta.annotation.PostConstruct;
import org.gymcrm.model.Trainee;
import org.gymcrm.model.Trainer;
import org.gymcrm.model.Training;
import org.gymcrm.model.TrainingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class StorageInitializer {
    private static final Logger logger = LoggerFactory.getLogger(StorageInitializer.class);

    private final InitialDataReader initialDataReader;
    private final InitialDataParser initialDataParser;
    private final InMemoryIdGenerator idGenerator;
    private final Resource initialDataFile;

    private final Map<Long, Trainee> traineeStorage;
    private final Map<Long, Trainer> trainerStorage;
    private final Map<Long, Training> trainingStorage;
    private final Map<Long, TrainingType> trainingTypeStorage;

    public StorageInitializer(
            InitialDataReader initialDataReader,
            InitialDataParser initialDataParser,
            InMemoryIdGenerator idGenerator,
            @Value("${initial.data.file}") Resource initialDataFile,
            @Qualifier("traineeStorage") Map<Long, Trainee> traineeStorage,
            @Qualifier("trainerStorage") Map<Long, Trainer> trainerStorage,
            @Qualifier("trainingStorage") Map<Long, Training> trainingStorage,
            @Qualifier("trainingTypeStorage") Map<Long, TrainingType> trainingTypeStorage
    ) {
        this.initialDataReader = initialDataReader;
        this.initialDataParser = initialDataParser;
        this.idGenerator = idGenerator;
        this.initialDataFile = initialDataFile;
        this.traineeStorage = traineeStorage;
        this.trainerStorage = trainerStorage;
        this.trainingStorage = trainingStorage;
        this.trainingTypeStorage = trainingTypeStorage;
    }

    @PostConstruct
    public void loadInitialData() {
        logger.info("Starting storage initialization from file: {}", initialDataFile.getFilename());

        List<String> lines = initialDataReader.readLines(initialDataFile);

        for (String line : lines) {
            String[] parts = line.split(";", -1);
            String recordType = parts[0];

            switch (recordType) {
                case "TRAINING_TYPE" -> {
                    TrainingType type = initialDataParser.parseTrainingType(parts);
                    trainingTypeStorage.put(type.getId(), type);
                }
                case "TRAINEE" -> {
                    Trainee trainee = initialDataParser.parseTrainee(parts);
                    traineeStorage.put(trainee.getUserId(), trainee);
                    idGenerator.initializeMaxTraineeId(trainee.getUserId());
                }
                case "TRAINER" -> {
                    Trainer trainer = initialDataParser.parseTrainer(parts, trainingTypeStorage);
                    trainerStorage.put(trainer.getUserId(), trainer);
                    idGenerator.initializeMaxTrainerId(trainer.getUserId());
                }
                case "TRAINING" -> {
                    Training training = initialDataParser.parseTraining(parts);
                    trainingStorage.put(training.getId(), training);
                    idGenerator.initializeMaxTrainingId(training.getId());
                }
                default -> logger.warn("Unknown record type discovered during parsing: {}", recordType);
            }
        }

        logger.info("Storage initialization finished successfully. Loaded components metrics:");
        logger.info("Types: {}, Trainees: {}, Trainers: {}, Trainings: {}",
                trainingTypeStorage.size(), traineeStorage.size(), trainerStorage.size(), trainingStorage.size());
    }
}