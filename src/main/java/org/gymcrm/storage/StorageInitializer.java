package org.gymcrm.storage;

import org.gymcrm.model.Trainee;
import org.gymcrm.model.Trainer;
import org.gymcrm.model.Training;
import org.gymcrm.model.TrainingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class StorageInitializer implements BeanPostProcessor {
    private static final Logger logger = LoggerFactory.getLogger(StorageInitializer.class);

    private final InitialDataReader initialDataReader;
    private final InitialDataParser initialDataParser;

    private Resource initialDataFile;

    private Map<Long, Trainee> traineeStorage;
    private Map<Long, Trainer> trainerStorage;
    private Map<Long, Training> trainingStorage;
    private Map<Long, TrainingType> trainingTypeStorage;

    private boolean initialized = false;

    public StorageInitializer(InitialDataReader initialDataReader, InitialDataParser initialDataParser) {
        this.initialDataReader = initialDataReader;
        this.initialDataParser = initialDataParser;
    }

    @Value("${initial.data.file}")
    public void setInitialDataFile(Resource initialDataFile) {
        this.initialDataFile = initialDataFile;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        switch (beanName) {
            case "traineeStorage" -> traineeStorage = (Map<Long, Trainee>) bean;
            case "trainerStorage" -> trainerStorage = (Map<Long, Trainer>) bean;
            case "trainingStorage" -> trainingStorage = (Map<Long, Training>) bean;
            case "trainingTypeStorage" -> trainingTypeStorage = (Map<Long, TrainingType>) bean;
        }

        if (!initialized && allStoragesAreReady()) {
            loadInitialData();
            initialized = true;
        }

        return bean;
    }

    private boolean allStoragesAreReady() {
        return traineeStorage != null
                && trainerStorage != null
                && trainingStorage != null
                && trainingTypeStorage != null;
    }

    private void loadInitialData() {
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
                }
                case "TRAINER" -> {
                    Trainer trainer = initialDataParser.parseTrainer(parts, trainingTypeStorage);
                    trainerStorage.put(trainer.getUserId(), trainer);
                }
                case "TRAINING" -> {
                    Training training = initialDataParser.parseTraining(parts);
                    trainingStorage.put(training.getId(), training);
                }
                default -> logger.warn("Unknown record type discovered during parsing: {}", recordType);
            }
        }

        logger.info("Storage initialization finished successfully. Loaded components metrics:");
        logger.info("Types: {}, Trainees: {}, Trainers: {}, Trainings: {}",
                trainingTypeStorage.size(), traineeStorage.size(), trainerStorage.size(), trainingStorage.size());
    }
}