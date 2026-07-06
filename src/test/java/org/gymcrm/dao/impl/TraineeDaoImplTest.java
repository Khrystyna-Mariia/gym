package org.gymcrm.dao.impl;

import org.gymcrm.config.AppConfig;
import org.gymcrm.dao.TraineeDao;
import org.gymcrm.model.Trainee;
import org.gymcrm.model.User;
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
class TraineeDaoImplTest {

    @Autowired
    private TraineeDao traineeDao;

    @Autowired
    private SessionFactory sessionFactory;

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    @Test
    void shouldSaveTrainee() {
        Trainee trainee = createTrainee(null, "John.Smith");

        Trainee savedTrainee = traineeDao.save(trainee);

        assertNotNull(savedTrainee.getId());
        assertEquals("John.Smith", savedTrainee.getUser().getUsername());
    }

    @Test
    void shouldUpdateTrainee() {
        Trainee trainee = createTrainee(null, "John.Smith");
        getCurrentSession().persist(trainee);
        getCurrentSession().flush();

        trainee.setAddress("Lviv");
        Trainee updatedTrainee = traineeDao.update(trainee);

        assertEquals("Lviv", updatedTrainee.getAddress());
    }

    @Test
    void shouldDeleteTraineeByIdWhenExists() {
        Trainee trainee = createTrainee(null, "John.Smith");
        getCurrentSession().persist(trainee);
        getCurrentSession().flush();

        boolean result = traineeDao.deleteById(trainee.getId());

        assertTrue(result);
        assertNull(getCurrentSession().get(Trainee.class, trainee.getId()));
    }

    @Test
    void shouldReturnFalseWhenDeletingNonExistingTrainee() {
        boolean result = traineeDao.deleteById(999L);
        assertFalse(result);
    }

    @Test
    void shouldFindTraineeById() {
        Trainee trainee = createTrainee(null, "John.Smith");
        getCurrentSession().persist(trainee);
        getCurrentSession().flush();

        Optional<Trainee> result = traineeDao.findById(trainee.getId());

        assertTrue(result.isPresent());
        assertEquals(trainee.getId(), result.get().getId());
    }

    @Test
    void shouldReturnEmptyOptionalWhenTraineeNotFound() {
        Optional<Trainee> result = traineeDao.findById(999L);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindAllTrainees() {
        getCurrentSession().persist(createTrainee(null, "John.Smith"));
        getCurrentSession().persist(createTrainee(null, "Anna.Brown"));
        getCurrentSession().flush();

        List<Trainee> result = traineeDao.findAll();

        assertTrue(result.size() >= 2);
    }

    @Test
    void shouldReturnTrueWhenUsernameExists() {
        Trainee trainee = createTrainee(null, "unique.user");
        getCurrentSession().persist(trainee);
        getCurrentSession().flush();

        boolean exists = traineeDao.existsByUsername("unique.user");
        assertTrue(exists);
    }

    @Test
    void shouldFindByUsername() {
        Trainee trainee = createTrainee(null, "find.me");
        getCurrentSession().persist(trainee);
        getCurrentSession().flush();

        Optional<Trainee> result = traineeDao.findByUsername("find.me");

        assertTrue(result.isPresent());
        assertEquals("find.me", result.get().getUser().getUsername());
    }

    private Trainee createTrainee(Long id, String username) {
        User user = new User(id, "First", "Last", username, "password123", true);
        return new Trainee(id, LocalDate.of(2000, 1, 1), "Kyiv", user, new HashSet<>(), new ArrayList<>());
    }
}