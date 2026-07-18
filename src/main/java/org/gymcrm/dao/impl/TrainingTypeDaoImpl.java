package org.gymcrm.dao.impl;

import org.gymcrm.dao.TrainingTypeDao;
import org.gymcrm.model.TrainingType;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainingTypeDaoImpl implements TrainingTypeDao {
    private static final Logger logger = LoggerFactory.getLogger(TrainingTypeDaoImpl.class);

    private final SessionFactory sessionFactory;

    public TrainingTypeDaoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<TrainingType> findAll() {
        logger.debug("Fetching all training types");
        return sessionFactory.getCurrentSession()
                .createQuery("from TrainingType", TrainingType.class)
                .getResultList();
    }

    @Override
    public Optional<TrainingType> findById(Long id) {
        logger.debug("Fetching training type by ID: {}", id);
        return Optional.ofNullable(sessionFactory.getCurrentSession().get(TrainingType.class, id));
    }
}