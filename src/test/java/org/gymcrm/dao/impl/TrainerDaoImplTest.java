package org.gymcrm.dao.impl;

import org.gymcrm.config.AppConfig;
import org.gymcrm.dao.TrainerDao;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(AppConfig.class)
@Transactional
class TrainerDaoImplTest {

    @Autowired
    private TrainerDao trainerDao;

    @Autowired
    private SessionFactory sessionFactory;

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    @Test
    void shouldSaveTrainer() {
        Trainer trainer = createTrainer(null, "Michael.Green");

        Trainer savedTrainer = trainerDao.save(trainer);

        assertNotNull(savedTrainer.getId());
    }

    @Test
    void shouldUpdateTrainer() {
        Trainer trainer = createTrainer(null, "Michael.Green");
        getCurrentSession().persist(trainer);
        getCurrentSession().flush();

        trainer.getUser().setFirstName("UpdatedName");
        Trainer updatedTrainer = trainerDao.update(trainer);

        assertEquals("UpdatedName", updatedTrainer.getUser().getFirstName());
    }

    @Test
    void shouldFindTrainerById() {
        Trainer trainer = createTrainer(null, "Michael.Green");
        getCurrentSession().persist(trainer);
        getCurrentSession().flush();

        Optional<Trainer> result = trainerDao.findById(trainer.getId());

        assertTrue(result.isPresent());
        assertEquals(trainer.getId(), result.get().getId());
    }

    @Test
    void shouldFindAllTrainers() {
        getCurrentSession().persist(createTrainer(null, "Michael.Green"));
        getCurrentSession().persist(createTrainer(null, "Olivia.White"));
        getCurrentSession().flush();

        List<Trainer> result = trainerDao.findAll();
        assertTrue(result.size() >= 2);
    }

    @Test
    void shouldReturnTrueWhenTrainerUsernameExists() {
        Trainer trainer = createTrainer(null, "exists.trainer");
        getCurrentSession().persist(trainer);
        getCurrentSession().flush();

        boolean exists = trainerDao.existsByUsername("exists.trainer");
        assertTrue(exists);
    }

    @Test
    void shouldFindByUsername() {
        Trainer trainer = createTrainer(null, "find.trainer");
        getCurrentSession().persist(trainer);
        getCurrentSession().flush();

        Optional<Trainer> result = trainerDao.findByUsername("find.trainer");

        assertTrue(result.isPresent());
        assertEquals("find.trainer", result.get().getUser().getUsername());
    }

    @Test
    void shouldFindTrainersNotAssignedToTrainee() {
        Session session = getCurrentSession();

        TrainingType type = session.createQuery(
                        "FROM TrainingType WHERE trainingTypeName = :name", TrainingType.class)
                .setParameter("name", TrainingTypeEnum.FITNESS)
                .uniqueResult();

        Trainer assignedTrainer = createTrainer(null, "assigned.coach");
        assignedTrainer.setSpecialization(type);
        session.persist(assignedTrainer);

        Trainer unassignedTrainer = createTrainer(null, "free.coach");
        unassignedTrainer.setSpecialization(type);
        session.persist(unassignedTrainer);

        User traineeUser = new User(null, "TraineeFirst", "TraineeLast", "john.smith", "pass", true);
        Trainee trainee = new Trainee(null, LocalDate.now(), "Address", traineeUser, new HashSet<>(), new ArrayList<>());
        trainee.getTrainers().add(assignedTrainer);

        session.persist(trainee);
        session.flush();

        List<Trainer> result = trainerDao.findTrainersNotAssignedToTrainee("john.smith");

        boolean containsFree = result.stream().anyMatch(t -> t.getUser().getUsername().equals("free.coach"));
        boolean containsAssigned = result.stream().anyMatch(t -> t.getUser().getUsername().equals("assigned.coach"));

        assertTrue(containsFree);
        assertFalse(containsAssigned);
    }

    private Trainer createTrainer(Long id, String username) {
        Session session = getCurrentSession();

        User user = new User(null, "First", "Last", username, "password123", true);

        TrainingType type = session.createQuery(
                        "FROM TrainingType WHERE trainingTypeName = :name", TrainingType.class)
                .setParameter("name", TrainingTypeEnum.FITNESS)
                .uniqueResultOptional()
                .orElseGet(() -> {
                    TrainingType newType = new TrainingType(null, TrainingTypeEnum.FITNESS);
                    session.persist(newType);
                    return newType;
                });

        return new Trainer(id, type, user, new HashSet<>());
    }
}