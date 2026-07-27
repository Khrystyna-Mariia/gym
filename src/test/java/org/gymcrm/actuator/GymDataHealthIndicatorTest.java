package org.gymcrm.actuator;

import org.gymcrm.dao.TraineeDao;
import org.gymcrm.dao.TrainerDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GymDataHealthIndicatorTest {

    @Mock private TraineeDao traineeDao;
    @Mock private TrainerDao trainerDao;
    @Mock private PlatformTransactionManager transactionManager;
    @Mock private TransactionStatus transactionStatus;

    private GymDataHealthIndicator indicator;

    @BeforeEach
    void setUp() {
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);
        indicator = new GymDataHealthIndicator(traineeDao, trainerDao, transactionManager);
    }

    @Test
    void health_returnsUpWhenTraineesExist() {
        when(traineeDao.count()).thenReturn(1L);
        when(trainerDao.count()).thenReturn(0L);

        Health health = indicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals(1L, health.getDetails().get("traineeCount"));
        assertEquals(0L, health.getDetails().get("trainerCount"));
    }

    @Test
    void health_returnsUpWhenTrainersExist() {
        when(traineeDao.count()).thenReturn(0L);
        when(trainerDao.count()).thenReturn(1L);

        Health health = indicator.health();

        assertEquals(Status.UP, health.getStatus());
    }

    @Test
    void health_returnsDownWhenNoDataExists() {
        when(traineeDao.count()).thenReturn(0L);
        when(trainerDao.count()).thenReturn(0L);

        Health health = indicator.health();

        assertEquals(Status.DOWN, health.getStatus());
    }

    @Test
    void health_returnsDownWithExceptionWhenDaoThrows() {
        when(traineeDao.count()).thenThrow(new RuntimeException("DB connection lost"));

        Health health = indicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertTrue(health.getDetails().get("error").toString().contains("DB connection lost"));
    }
}