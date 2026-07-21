package org.gymcrm.dao.impl;

import org.gymcrm.config.AppConfig;
import org.gymcrm.dao.TrainingTypeDao;
import org.gymcrm.model.TrainingType;
import org.gymcrm.model.TrainingTypeEnum;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(AppConfig.class)
@Transactional
class TrainingTypeDaoImplTest {

    @Autowired
    private TrainingTypeDao trainingTypeDao;

    @Autowired
    private SessionFactory sessionFactory;

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    @Test
    void findAll_shouldReturnAllTypes() {
        Session session = getCurrentSession();

        ensureTrainingTypeExists(session, TrainingTypeEnum.FITNESS);
        ensureTrainingTypeExists(session, TrainingTypeEnum.YOGA);
        session.flush();

        List<TrainingType> result = trainingTypeDao.findAll();
        assertTrue(result.size() >= 2);
    }

    @Test
    void findById_shouldReturnEmptyWhenNotExists() {
        Optional<TrainingType> result = trainingTypeDao.findById(999L);
        assertTrue(result.isEmpty());
    }

    @Test
    void findById_shouldReturnTypeWhenExists() {
        Session session = getCurrentSession();
        TrainingType type = ensureTrainingTypeExists(session, TrainingTypeEnum.ZUMBA);
        session.flush();

        Optional<TrainingType> result = trainingTypeDao.findById(type.getId());

        assertTrue(result.isPresent());
        assertEquals(TrainingTypeEnum.ZUMBA, result.get().getTrainingTypeName());
    }

    private TrainingType ensureTrainingTypeExists(Session session, TrainingTypeEnum typeEnum) {
        return session.createQuery(
                        "FROM TrainingType WHERE trainingTypeName = :name", TrainingType.class)
                .setParameter("name", typeEnum)
                .uniqueResultOptional()
                .orElseGet(() -> {
                    TrainingType newType = new TrainingType(null, typeEnum);
                    session.persist(newType);
                    return newType;
                });
    }
}