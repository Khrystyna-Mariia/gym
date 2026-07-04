package org.gymcrm.dao.impl;

import org.gymcrm.dao.TrainingDao;
import org.gymcrm.model.Training;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class TrainingDaoImpl implements TrainingDao {
    private static final Logger logger = LoggerFactory.getLogger(TrainingDaoImpl.class);

    private final SessionFactory sessionFactory;

    public TrainingDaoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public Training save(Training training) {
        logger.debug("Persisting new training: {}", training.getTrainingName());
        getCurrentSession().persist(training);
        return training;
    }

    @Override
    public Optional<Training> findById(Long id) {
        logger.debug("Fetching training by ID: {}", id);
        return Optional.ofNullable(getCurrentSession().get(Training.class, id));
    }

    @Override
    public List<Training> findAll() {
        logger.debug("Fetching all trainings");
        return getCurrentSession().createQuery("from Training", Training.class).getResultList();
    }

    @Override
    public List<Training> findTraineeTrainings(String username, LocalDate fromDate, LocalDate toDate, String trainerName, String trainingTypeName) {
        logger.debug("Filtering trainee trainings for username '{}'", username);

        StringBuilder hql = new StringBuilder("FROM Training t WHERE LOWER(t.trainee.user.username) = LOWER(:username)");

        if (fromDate != null) hql.append(" AND t.trainingDate >= :fromDate");
        if (toDate != null) hql.append(" AND t.trainingDate <= :toDate");
        if (trainerName != null && !trainerName.trim().isEmpty()) {
            hql.append(" AND (LOWER(t.trainer.user.firstName) LIKE LOWER(:trainerName) OR LOWER(t.trainer.user.lastName) LIKE LOWER(:trainerName))");
        }
        if (trainingTypeName != null && !trainingTypeName.trim().isEmpty()) {
            hql.append(" AND LOWER(t.trainingType.trainingTypeName) = LOWER(:typeName)");
        }

        Query<Training> query = getCurrentSession().createQuery(hql.toString(), Training.class)
                .setParameter("username", username);

        if (fromDate != null) query.setParameter("fromDate", fromDate);
        if (toDate != null) query.setParameter("toDate", toDate);
        if (trainerName != null && !trainerName.trim().isEmpty()) query.setParameter("trainerName", "%" + trainerName + "%");
        if (trainingTypeName != null && !trainingTypeName.trim().isEmpty()) query.setParameter("typeName", trainingTypeName);

        return query.getResultList();
    }

    @Override
    public List<Training> findTrainerTrainings(String username, LocalDate fromDate, LocalDate toDate, String traineeName) {
        logger.debug("Filtering trainer trainings for username '{}'", username);

        StringBuilder hql = new StringBuilder("FROM Training t WHERE LOWER(t.trainer.user.username) = LOWER(:username)");

        if (fromDate != null) hql.append(" AND t.trainingDate >= :fromDate");
        if (toDate != null) hql.append(" AND t.trainingDate <= :toDate");
        if (traineeName != null && !traineeName.trim().isEmpty()) {
            hql.append(" AND (LOWER(t.trainee.user.firstName) LIKE LOWER(:traineeName) OR LOWER(t.trainee.user.lastName) LIKE LOWER(:traineeName))");
        }

        Query<Training> query = getCurrentSession().createQuery(hql.toString(), Training.class)
                .setParameter("username", username);

        if (fromDate != null) query.setParameter("fromDate", fromDate);
        if (toDate != null) query.setParameter("toDate", toDate);
        if (traineeName != null && !traineeName.trim().isEmpty()) query.setParameter("traineeName", "%" + traineeName + "%");

        return query.getResultList();
    }
}