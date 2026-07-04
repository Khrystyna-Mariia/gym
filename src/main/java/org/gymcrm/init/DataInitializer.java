package org.gymcrm.init;

import jakarta.annotation.PostConstruct;
import org.gymcrm.model.Trainee;
import org.gymcrm.model.Trainer;
import org.gymcrm.model.Training;
import org.gymcrm.model.TrainingType;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DataInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final InitialDataReader initialDataReader;
    private final InitialDataParser initialDataParser;
    private final SessionFactory sessionFactory;
    private final Resource initialDataFile;

    public DataInitializer(
            InitialDataReader initialDataReader,
            InitialDataParser initialDataParser,
            SessionFactory sessionFactory,
            @Value("${initial.data.file}") Resource initialDataFile
    ) {
        this.initialDataReader = initialDataReader;
        this.initialDataParser = initialDataParser;
        this.sessionFactory = sessionFactory;
        this.initialDataFile = initialDataFile;
    }

    @PostConstruct
    public void loadInitialData() {
        try (Session session = sessionFactory.openSession()) {
            Long userCount = session.createQuery("select count(u) from User u", Long.class).getSingleResult();
            if (userCount > 0) {
                logger.info("Database already contains data ({} users found). Skipping storage initialization.", userCount);
                return;
            }
        } catch (Exception e) {
            logger.error("Failed to check initial database state", e);
            throw new IllegalStateException("Database state verification failed", e);
        }

        logger.info("Starting storage initialization from file: {}", initialDataFile.getFilename());
        List<String> lines = initialDataReader.readLines(initialDataFile);

        Map<Long, TrainingType> trainingTypeMap = new HashMap<>();
        Map<Long, Trainee> traineeMap = new HashMap<>();
        Map<Long, Trainer> trainerMap = new HashMap<>();

        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            for (String line : lines) {
                String[] parts = line.split(";", -1);

                if (parts.length == 0 || parts[0].isBlank()) {
                    logger.warn("Skipping empty or invalid line during storage initialization");
                    continue;
                }

                String recordType = parts[0];

                switch (recordType) {
                    case "TRAINING_TYPE" -> {
                        Long fileId = initialDataParser.parseLong(parts[1], "training type id");
                        TrainingType type = initialDataParser.parseTrainingType(parts);

                        type.setId(null);

                        session.persist(type);
                        trainingTypeMap.put(fileId, type);
                    }
                    case "TRAINEE" -> {
                        Long fileId = initialDataParser.parseLong(parts[1], "trainee id");
                        Trainee trainee = initialDataParser.parseTrainee(parts);

                        session.persist(trainee);
                        traineeMap.put(fileId, trainee);
                    }
                    case "TRAINER" -> {
                        Long fileId = initialDataParser.parseLong(parts[1], "trainer id");
                        Long fileSpecId = initialDataParser.parseLong(parts[7], "specialization id");

                        Trainer trainer = initialDataParser.parseTrainer(parts);
                        TrainingType managedSpec = trainingTypeMap.get(fileSpecId);

                        if (managedSpec == null) {
                            throw new IllegalStateException("TrainingType with file ID " + fileSpecId + " not found for trainer");
                        }

                        trainer.setSpecialization(managedSpec);
                        session.persist(trainer);
                        trainerMap.put(fileId, trainer);
                    }
                    case "TRAINING" -> {
                        Training training = initialDataParser.parseTraining(parts);

                        Long fileTraineeId = initialDataParser.parseLong(parts[2], "trainee id");
                        Long fileTrainerId = initialDataParser.parseLong(parts[3], "trainer id");
                        Long fileTypeId = initialDataParser.parseLong(parts[5], "training type id");

                        Trainee managedTrainee = traineeMap.get(fileTraineeId);
                        Trainer managedTrainer = trainerMap.get(fileTrainerId);
                        TrainingType managedType = trainingTypeMap.get(fileTypeId);

                        if (managedTrainee == null || managedTrainer == null || managedType == null) {
                            throw new IllegalStateException("Failed to bind relations for Training: missing referenced entities in seed data");
                        }

                        training.setTrainee(managedTrainee);
                        training.setTrainer(managedTrainer);
                        training.setTrainingType(managedType);

                        training.setId(null);

                        session.persist(training);
                    }
                    default -> logger.warn("Unknown record type discovered during parsing: {}", recordType);
                }
            }
            tx.commit();
            logger.info("Database seeding finished successfully.");
        } catch (Exception e) {
            logger.error("Database initialization aborted due to a critical error", e);
            throw new IllegalStateException("Database seeding failed", e);
        }
    }
}