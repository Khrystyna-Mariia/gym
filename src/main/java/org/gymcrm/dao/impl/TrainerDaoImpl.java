package org.gymcrm.dao.impl;

import org.gymcrm.dao.TrainerDao;
import org.gymcrm.model.Trainer;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;

@Repository
public class TrainerDaoImpl implements TrainerDao {
    private static final Logger logger = LoggerFactory.getLogger(TrainerDaoImpl.class);

    private final SessionFactory sessionFactory;

    public TrainerDaoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public Trainer save(Trainer trainer) {
        logger.debug("Persisting new trainer profile for user: {}", trainer.getUser().getUsername());
        getCurrentSession().persist(trainer);
        return trainer;
    }

    @Override
    public Trainer update(Trainer trainer) {
        logger.debug("Merging trainer profile with ID: {}", trainer.getId());
        return getCurrentSession().merge(trainer);
    }

    @Override
    public Optional<Trainer> findById(Long id) {
        logger.debug("Fetching trainer by ID: {}", id);
        return Optional.ofNullable(getCurrentSession().get(Trainer.class, id));
    }

    @Override
    public List<Trainer> findAll() {
        logger.debug("Fetching all trainers from database");
        return getCurrentSession().createQuery("from Trainer", Trainer.class).getResultList();
    }

    @Override
    public boolean existsByUsername(String username) {
        logger.debug("Checking existence of trainer username: {}", username);
        String hql = "select count(t) from Trainer t where lower(t.user.username) = lower(:username)";
        Long count = getCurrentSession().createQuery(hql, Long.class)
                .setParameter("username", username)
                .uniqueResult();
        return count != null && count > 0;
    }

    @Override
    public Optional<Trainer> findByUsername(String username) {
        logger.debug("Finding trainer by username: {}", username);
        String hql = "FROM Trainer t JOIN FETCH t.user WHERE LOWER(t.user.username) = LOWER(:username)";
        return getCurrentSession().createQuery(hql, Trainer.class)
                .setParameter("username", username)
                .uniqueResultOptional();
    }

    @Override
    public List<Trainer> findTrainersNotAssignedToTrainee(String traineeUsername) {
        logger.debug("Finding trainers not assigned to trainee: {}", traineeUsername);
        String hql = "FROM Trainer t WHERE t.id NOT IN " +
                "(SELECT tr.id FROM Trainee tn JOIN tn.trainers tr WHERE LOWER(tn.user.username) = LOWER(:username))";
        return getCurrentSession().createQuery(hql, Trainer.class)
                .setParameter("username", traineeUsername)
                .getResultList();
    }

    @Override
    public List<Trainer> findByUsernames(List<String> usernames) {
        logger.debug("Finding trainers by usernames list size: {}", usernames != null ? usernames.size() : 0);
        if (usernames == null || usernames.isEmpty()) {
            return emptyList();
        }

        List<String> lowerUsernames = usernames.stream()
                .map(String::toLowerCase)
                .toList();

        String hql = "FROM Trainer t JOIN FETCH t.user WHERE LOWER(t.user.username) IN (:usernames)";
        return getCurrentSession().createQuery(hql, Trainer.class)
                .setParameterList("usernames", lowerUsernames)
                .getResultList();
    }
}