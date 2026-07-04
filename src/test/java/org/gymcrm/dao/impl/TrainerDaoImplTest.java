package org.gymcrm.dao.impl;

import org.gymcrm.model.Trainer;
import org.gymcrm.model.TrainingType;
import org.gymcrm.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerDaoImplTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private Query<Trainer> trainerQuery;

    @Mock
    private Query<Long> countQuery;

    @InjectMocks
    private TrainerDaoImpl trainerDao;

    @BeforeEach
    void setUp() {
        lenient().when(sessionFactory.getCurrentSession()).thenReturn(session);
    }

    @Test
    void shouldSaveTrainer() {
        Trainer trainer = createTrainer(null, "Michael.Green");

        Trainer savedTrainer = trainerDao.save(trainer);

        verify(session).persist(trainer);
        assertEquals(trainer, savedTrainer);
    }

    @Test
    void shouldUpdateTrainer() {
        Trainer trainer = createTrainer(1L, "Michael.Green");
        when(session.merge(trainer)).thenReturn(trainer);

        Trainer updatedTrainer = trainerDao.update(trainer);

        verify(session).merge(trainer);
        assertEquals(trainer, updatedTrainer);
    }

    @Test
    void shouldFindTrainerById() {
        Long trainerId = 1L;
        Trainer trainer = createTrainer(trainerId, "Michael.Green");
        when(session.get(Trainer.class, trainerId)).thenReturn(trainer);

        Optional<Trainer> result = trainerDao.findById(trainerId);

        assertTrue(result.isPresent());
        assertEquals(trainer, result.get());
    }

    @Test
    void shouldReturnEmptyOptionalWhenTrainerNotFound() {
        Long trainerId = 99L;
        when(session.get(Trainer.class, trainerId)).thenReturn(null);

        Optional<Trainer> result = trainerDao.findById(trainerId);

        assertTrue(result.isEmpty());
        assertEquals(Optional.empty(), result);
    }

    @Test
    void shouldFindAllTrainers() {
        List<Trainer> trainers = List.of(createTrainer(1L, "Michael.Green"), createTrainer(2L, "Olivia.White"));
        when(session.createQuery("from Trainer", Trainer.class)).thenReturn(trainerQuery);
        when(trainerQuery.getResultList()).thenReturn(trainers);

        List<Trainer> result = trainerDao.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void shouldReturnTrueWhenTrainerUsernameExists() {
        String username = "michael.green";
        String hql = "select count(t) from Trainer t where lower(t.user.username) = lower(:username)";
        when(session.createQuery(hql, Long.class)).thenReturn(countQuery);
        when(countQuery.setParameter("username", username)).thenReturn(countQuery);
        when(countQuery.uniqueResult()).thenReturn(1L);

        boolean exists = trainerDao.existsByUsername(username);

        assertTrue(exists);
    }

    @Test
    void shouldFindByUsername() {
        String username = "michael.green";
        String hql = "FROM Trainer t JOIN FETCH t.user WHERE LOWER(t.user.username) = LOWER(:username)";
        Trainer trainer = createTrainer(1L, username);

        when(session.createQuery(hql, Trainer.class)).thenReturn(trainerQuery);
        when(trainerQuery.setParameter("username", username)).thenReturn(trainerQuery);
        when(trainerQuery.uniqueResultOptional()).thenReturn(Optional.of(trainer));

        Optional<Trainer> result = trainerDao.findByUsername(username);

        assertTrue(result.isPresent());
        assertEquals(trainer, result.get());
    }

    @Test
    void shouldFindTrainersNotAssignedToTrainee() {
        String traineeUsername = "john.smith";
        String hql = "FROM Trainer t WHERE t.id NOT IN " +
                "(SELECT tr.id FROM Trainee tn JOIN tn.trainers tr WHERE LOWER(tn.user.username) = LOWER(:username))";
        List<Trainer> unassigned = List.of(createTrainer(5L, "Coach.Alex"));

        when(session.createQuery(hql, Trainer.class)).thenReturn(trainerQuery);
        when(trainerQuery.setParameter("username", traineeUsername)).thenReturn(trainerQuery);
        when(trainerQuery.getResultList()).thenReturn(unassigned);

        List<Trainer> result = trainerDao.findTrainersNotAssignedToTrainee(traineeUsername);

        assertEquals(1, result.size());
        assertEquals("Coach.Alex", result.get(0).getUser().getUsername());
    }

    private Trainer createTrainer(Long id, String username) {
        User user = new User(id, "First", "Last", username, "password123", true);
        TrainingType type = new TrainingType(1L, "Fitness");
        return new Trainer(id, type, user, new HashSet<>());
    }
}