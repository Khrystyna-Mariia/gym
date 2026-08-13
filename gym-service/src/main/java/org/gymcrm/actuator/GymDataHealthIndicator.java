package org.gymcrm.actuator;

import org.gymcrm.dao.TraineeDao;
import org.gymcrm.dao.TrainerDao;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class GymDataHealthIndicator implements HealthIndicator {

    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final TransactionTemplate transactionTemplate;

    public GymDataHealthIndicator(TraineeDao traineeDao,
                                  TrainerDao trainerDao,
                                  PlatformTransactionManager transactionManager) {
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setReadOnly(true);
    }

    @Override
    public Health health() {
        try {
            return transactionTemplate.execute(status -> {
                long traineeCount = traineeDao.count();
                long trainerCount = trainerDao.count();

                Health.Builder builder = (traineeCount > 0 || trainerCount > 0) ? Health.up() : Health.down();
                return builder
                        .withDetail("traineeCount", traineeCount)
                        .withDetail("trainerCount", trainerCount)
                        .build();
            });
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}