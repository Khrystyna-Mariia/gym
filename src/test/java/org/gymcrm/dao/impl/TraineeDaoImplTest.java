package org.gymcrm.dao.impl;

import org.gymcrm.model.Trainee;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeDaoImplTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private Query<Trainee> traineeQuery;

    @Mock
    private Query<Long> countQuery;

    @InjectMocks
    private TraineeDaoImpl traineeDao;

    @BeforeEach
    void setUp() {
        lenient().when(sessionFactory.getCurrentSession()).thenReturn(session);
    }

    @Test
    void shouldSaveTrainee() {
        Trainee trainee = createTrainee(null, "John.Smith");

        Trainee savedTrainee = traineeDao.save(trainee);

        verify(session).persist(trainee);
        assertEquals(trainee, savedTrainee);
    }

    @Test
    void shouldUpdateTrainee() {
        Trainee trainee = createTrainee(1L, "John.Smith");
        when(session.merge(trainee)).thenReturn(trainee);

        Trainee updatedTrainee = traineeDao.update(trainee);

        verify(session).merge(trainee);
        assertEquals(trainee, updatedTrainee);
    }

    @Test
    void shouldDeleteTraineeByIdWhenExists() {
        Long traineeId = 1L;
        Trainee trainee = createTrainee(traineeId, "John.Smith");
        when(session.get(Trainee.class, traineeId)).thenReturn(trainee);

        boolean result = traineeDao.deleteById(traineeId);

        verify(session).remove(trainee);
        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenDeletingNonExistingTrainee() {
        Long traineeId = 99L;
        when(session.get(Trainee.class, traineeId)).thenReturn(null);

        boolean result = traineeDao.deleteById(traineeId);

        verify(session, never()).remove(any());
        assertFalse(result);
    }

    @Test
    void shouldFindTraineeById() {
        Long traineeId = 1L;
        Trainee trainee = createTrainee(traineeId, "John.Smith");
        when(session.get(Trainee.class, traineeId)).thenReturn(trainee);

        Optional<Trainee> result = traineeDao.findById(traineeId);

        assertTrue(result.isPresent());
        assertEquals(trainee, result.get());
    }

    @Test
    void shouldReturnEmptyOptionalWhenTraineeNotFound() {
        Long traineeId = 99L;
        when(session.get(Trainee.class, traineeId)).thenReturn(null);

        Optional<Trainee> result = traineeDao.findById(traineeId);

        assertTrue(result.isEmpty());
        assertEquals(Optional.empty(), result);
    }

    @Test
    void shouldFindAllTrainees() {
        List<Trainee> trainees = List.of(createTrainee(1L, "John.Smith"), createTrainee(2L, "Anna.Brown"));
        when(session.createQuery("from Trainee", Trainee.class)).thenReturn(traineeQuery);
        when(traineeQuery.getResultList()).thenReturn(trainees);

        List<Trainee> result = traineeDao.findAll();

        assertEquals(2, result.size());
        verify(traineeQuery).getResultList();
    }

    @Test
    void shouldReturnTrueWhenUsernameExists() {
        String username = "john.smith";
        String hql = "select count(t) from Trainee t where lower(t.user.username) = lower(:username)";
        when(session.createQuery(hql, Long.class)).thenReturn(countQuery);
        when(countQuery.setParameter("username", username)).thenReturn(countQuery);
        when(countQuery.uniqueResult()).thenReturn(1L);

        boolean exists = traineeDao.existsByUsername(username);

        assertTrue(exists);
    }

    @Test
    void shouldFindByUsername() {
        String username = "john.smith";
        String hql = "FROM Trainee t JOIN FETCH t.user WHERE LOWER(t.user.username) = LOWER(:username)";
        Trainee trainee = createTrainee(1L, username);

        when(session.createQuery(hql, Trainee.class)).thenReturn(traineeQuery);
        when(traineeQuery.setParameter("username", username)).thenReturn(traineeQuery);
        when(traineeQuery.uniqueResultOptional()).thenReturn(Optional.of(trainee));

        Optional<Trainee> result = traineeDao.findByUsername(username);

        assertTrue(result.isPresent());
        assertEquals(trainee, result.get());
    }

    private Trainee createTrainee(Long id, String username) {
        User user = new User(id, "First", "Last", username, "password123", true);
        return new Trainee(id, LocalDate.of(2000, 1, 1), "Kyiv", user, new HashSet<>(), new ArrayList<>());
    }
}