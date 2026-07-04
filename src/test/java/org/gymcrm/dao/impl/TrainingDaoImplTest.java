package org.gymcrm.dao.impl;

import org.gymcrm.model.Trainee;
import org.gymcrm.model.Trainer;
import org.gymcrm.model.Training;
import org.gymcrm.model.TrainingType;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingDaoImplTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private Query<Training> trainingQuery;

    @InjectMocks
    private TrainingDaoImpl trainingDao;

    @BeforeEach
    void setUp() {
        lenient().when(sessionFactory.getCurrentSession()).thenReturn(session);
    }

    @Test
    void shouldSaveTraining() {
        Training training = createTraining(null, "Morning Cardio");

        Training savedTraining = trainingDao.save(training);

        verify(session).persist(training);
        assertEquals(training, savedTraining);
    }

    @Test
    void shouldFindTrainingById() {
        Long trainingId = 1L;
        Training training = createTraining(trainingId, "Morning Cardio");
        when(session.get(Training.class, trainingId)).thenReturn(training);

        Optional<Training> result = trainingDao.findById(trainingId);

        assertTrue(result.isPresent());
        assertEquals(training, result.get());
    }

    @Test
    void shouldReturnEmptyOptionalWhenTrainingNotFound() {
        Long trainingId = 99L;
        when(session.get(Training.class, trainingId)).thenReturn(null);

        Optional<Training> result = trainingDao.findById(trainingId);

        assertTrue(result.isEmpty());
        assertEquals(Optional.empty(), result);
    }

    @Test
    void shouldFindAllTrainings() {
        List<Training> trainings = List.of(createTraining(1L, "Yoga"), createTraining(2L, "Cardio"));
        when(session.createQuery("from Training", Training.class)).thenReturn(trainingQuery);
        when(trainingQuery.getResultList()).thenReturn(trainings);

        List<Training> result = trainingDao.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void shouldFindTraineeTrainingsWithFilters() {
        String username = "john.smith";
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 12, 31);

        String expectedHql = "FROM Training t WHERE LOWER(t.trainee.user.username) = LOWER(:username) " +
                "AND t.trainingDate >= :fromDate AND t.trainingDate <= :toDate " +
                "AND (LOWER(t.trainer.user.firstName) LIKE LOWER(:trainerName) OR LOWER(t.trainer.user.lastName) LIKE LOWER(:trainerName)) " +
                "AND LOWER(t.trainingType.trainingTypeName) = LOWER(:typeName)";

        when(session.createQuery(expectedHql, Training.class)).thenReturn(trainingQuery);
        when(trainingQuery.setParameter("username", username)).thenReturn(trainingQuery);
        when(trainingQuery.setParameter("fromDate", from)).thenReturn(trainingQuery);
        when(trainingQuery.setParameter("toDate", to)).thenReturn(trainingQuery);
        when(trainingQuery.setParameter(eq("trainerName"), anyString())).thenReturn(trainingQuery);
        when(trainingQuery.setParameter("typeName", "Fitness")).thenReturn(trainingQuery);
        when(trainingQuery.getResultList()).thenReturn(List.of(createTraining(1L, "Filtered Training")));

        List<Training> result = trainingDao.findTraineeTrainings(username, from, to, "John", "Fitness");

        assertFalse(result.isEmpty());
        verify(trainingQuery).getResultList();
    }

    @Test
    void shouldFindTrainerTrainingsWithFilters() {
        String username = "michael.green";
        LocalDate from = LocalDate.of(2026, 6, 1);

        String expectedHql = "FROM Training t WHERE LOWER(t.trainer.user.username) = LOWER(:username) " +
                "AND t.trainingDate >= :fromDate";

        when(session.createQuery(expectedHql, Training.class)).thenReturn(trainingQuery);
        when(trainingQuery.setParameter("username", username)).thenReturn(trainingQuery);
        when(trainingQuery.setParameter("fromDate", from)).thenReturn(trainingQuery);
        when(trainingQuery.getResultList()).thenReturn(List.of(createTraining(1L, "Trainer Session")));

        List<Training> result = trainingDao.findTrainerTrainings(username, from, null, null);

        assertEquals(1, result.size());
    }

    private Training createTraining(Long id, String name) {
        return new Training(
                id,
                new Trainee(),
                new Trainer(),
                name,
                new TrainingType(),
                LocalDate.of(2026, 7, 4),
                60
        );
    }
}