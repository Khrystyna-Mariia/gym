package org.gymcrm.dao.impl;

import org.gymcrm.config.AppConfig;
import org.gymcrm.dao.TrainingDao;
import org.gymcrm.model.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(AppConfig.class)
@Transactional
class TrainingDaoImplTest {

    @Autowired
    private TrainingDao trainingDao;

    @Autowired
    private SessionFactory sessionFactory;

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    @Test
    void shouldSaveTraining() {
        Training training = createAndPersistFullTraining("trainee1", "trainer1", "Cardio", LocalDate.now());
        assertNotNull(training.getId());
    }

    @Test
    void shouldFindTrainingById() {
        Training training = createAndPersistFullTraining("trainee2", "trainer2", "Yoga", LocalDate.now());

        var result = trainingDao.findById(training.getId());

        assertTrue(result.isPresent());
        assertEquals(training.getTrainingName(), result.get().getTrainingName());
    }

    @Test
    void shouldFindAllTrainings() {
        createAndPersistFullTraining("trainee3", "trainer3", "T1", LocalDate.now());
        createAndPersistFullTraining("trainee4", "trainer4", "T2", LocalDate.now());

        List<Training> result = trainingDao.findAll();
        assertTrue(result.size() >= 2);
    }

    @Test
    void shouldFindTraineeTrainingsWithFilters() {
        String traineeUsername = "john.smith";
        LocalDate targetDate = LocalDate.of(2026, 5, 15);

        Training matching = createAndPersistFullTraining(traineeUsername, "jack.trainer", "Fitness Class", targetDate);

        matching.getTrainer().getUser().setFirstName("Jack");

        matching.getTrainingType().setTrainingTypeName(TrainingTypeEnum.FITNESS);

        getCurrentSession().flush();

        List<Training> result = trainingDao.findTraineeTrainings(
                traineeUsername,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                "Jack",
                "Fitness"
        );

        assertFalse(result.isEmpty());
        assertEquals("Fitness Class", result.get(0).getTrainingName());
    }

    @Test
    void shouldFindTrainerTrainingsWithFilters() {
        String trainerUsername = "michael.green";
        LocalDate targetDate = LocalDate.of(2026, 7, 10);

        createAndPersistFullTraining("some.trainee", trainerUsername, "Heavy Lift", targetDate);

        List<Training> result = trainingDao.findTrainerTrainings(
                trainerUsername,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 30),
                "TraineeFN"
        );

        assertEquals(1, result.size());
        assertEquals("Heavy Lift", result.get(0).getTrainingName());
    }

    private Training createAndPersistFullTraining(String traineeUser, String trainerUser, String trainingName, LocalDate date) {
        Session session = getCurrentSession();

        User u1 = new User(null, "TraineeFN", "TraineeLN", traineeUser, "pass", true);
        Trainee trainee = new Trainee(null, LocalDate.of(2000, 1, 1), "Kyiv", u1, new HashSet<>(), new ArrayList<>());
        session.persist(trainee);

        TrainingType type = session.createQuery(
                        "FROM TrainingType WHERE trainingTypeName = :name", TrainingType.class)
                .setParameter("name", TrainingTypeEnum.FITNESS)
                .uniqueResultOptional()
                .orElseGet(() -> {
                    TrainingType newType = new TrainingType(null, TrainingTypeEnum.FITNESS);
                    session.persist(newType);
                    return newType;
                });

        User u2 = new User(null, "TrainerFN", "TrainerLN", trainerUser, "pass", true);
        Trainer trainer = new Trainer(null, type, u2, new HashSet<>());
        session.persist(trainer);

        Training training = new Training(null, trainee, trainer, trainingName, type, date, 60);
        session.persist(training);
        session.flush();

        return training;
    }

    @Test
    void shouldFindTraineeTrainingsWithNullFilters() {
        String traineeUsername = "john.smith";
        createAndPersistFullTraining(traineeUsername, "jack.trainer", "Fitness Class", LocalDate.now());

        List<Training> result = trainingDao.findTraineeTrainings(
                traineeUsername, null, null, null, null
        );

        assertFalse(result.isEmpty());
    }

    @Test
    void shouldFindTrainerTrainingsWithNullFilters() {
        String trainerUsername = "michael.green";
        createAndPersistFullTraining("some.trainee", trainerUsername, "Heavy Lift", LocalDate.now());

        List<Training> result = trainingDao.findTrainerTrainings(
                trainerUsername, null, null, null
        );

        assertFalse(result.isEmpty());
    }
}