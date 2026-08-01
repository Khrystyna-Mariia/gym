package org.gymcrm.dao.impl;

import org.gymcrm.dao.UserDao;
import org.gymcrm.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserDaoImpl implements UserDao {
    private static final Logger logger = LoggerFactory.getLogger(UserDaoImpl.class);

    private final SessionFactory sessionFactory;

    public UserDaoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        logger.debug("Fetching user profile by username for authentication: {}", username);
        String hql = "FROM User u WHERE LOWER(u.username) = LOWER(:username)";
        return getCurrentSession().createQuery(hql, User.class)
                .setParameter("username", username)
                .uniqueResultOptional();
    }
}