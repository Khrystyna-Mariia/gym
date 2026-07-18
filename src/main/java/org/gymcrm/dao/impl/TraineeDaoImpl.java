package org.gymcrm.dao.impl;

import org.gymcrm.dao.TraineeDao;
import org.gymcrm.model.Trainee;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TraineeDaoImpl implements TraineeDao {
    private static final Logger logger = LoggerFactory.getLogger(TraineeDaoImpl.class);

    private final SessionFactory sessionFactory;

    public TraineeDaoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public Trainee save(Trainee trainee) {
        logger.debug("Persisting new trainee profile for user: {}", trainee.getUser().getUsername());
        getCurrentSession().persist(trainee);
        return trainee;
    }

    @Override
    public Trainee update(Trainee trainee) {
        logger.debug("Merging trainee profile with ID: {}", trainee.getId());
        return getCurrentSession().merge(trainee);
    }

    @Override
    public boolean deleteById(Long id) {
        logger.debug("Attempting to delete trainee with ID: {}", id);
        Trainee trainee = getCurrentSession().get(Trainee.class, id);
        if (trainee != null) {
            getCurrentSession().remove(trainee);
            logger.info("Trainee with ID: {} successfully deleted", id);
            return true;
        }
        logger.warn("Trainee with ID: {} not found for deletion", id);
        return false;
    }

    @Override
    public Optional<Trainee> findById(Long id) {
        logger.debug("Fetching trainee by ID: {}", id);
        return Optional.ofNullable(getCurrentSession().get(Trainee.class, id));
    }

    @Override
    public List<Trainee> findAll() {
        logger.debug("Fetching all trainees with their user profiles from database");
        return getCurrentSession()
                .createQuery("from Trainee t join fetch t.user", Trainee.class)
                .getResultList();
    }

    @Override
    public boolean existsByUsername(String username) {
        logger.debug("Checking existence of trainee username: {}", username);
        String hql = "select count(t) from Trainee t where lower(t.user.username) = lower(:username)";
        Long count = getCurrentSession().createQuery(hql, Long.class)
                .setParameter("username", username)
                .uniqueResult();
        return count != null && count > 0;
    }

    @Override
    public Optional<Trainee> findByUsername(String username) {
        logger.debug("Finding trainee by username: {}", username);
        String hql = "FROM Trainee t " +
                "JOIN FETCH t.user " +
                "LEFT JOIN FETCH t.trainers " +
                "WHERE LOWER(t.user.username) = LOWER(:username)";
        return getCurrentSession().createQuery(hql, Trainee.class)
                .setParameter("username", username)
                .uniqueResultOptional();
    }
}