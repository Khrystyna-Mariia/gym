package org.gymcrm.config;

import org.gymcrm.dao.TraineeDao;
import org.gymcrm.dao.TrainerDao;
import org.gymcrm.dao.TrainingDao;
import org.gymcrm.facade.GymFacade;
import org.gymcrm.model.Trainee;
import org.gymcrm.model.Trainer;
import org.gymcrm.model.Training;
import org.gymcrm.model.TrainingType;
import org.gymcrm.service.TraineeService;
import org.gymcrm.service.TrainerService;
import org.gymcrm.service.TrainingService;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AppConfigTest {

    @Test
    void shouldStartSpringContextSuccessfully() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(AppConfig.class)) {

            assertNotNull(context);
            assertTrue(context.isActive());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldInitializeStorageWithDataFromFile() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(AppConfig.class)) {

            Map<Long, Trainee> traineeStorage =
                    (Map<Long, Trainee>) context.getBean("traineeStorage", Map.class);

            Map<Long, Trainer> trainerStorage =
                    (Map<Long, Trainer>) context.getBean("trainerStorage", Map.class);

            Map<Long, Training> trainingStorage =
                    (Map<Long, Training>) context.getBean("trainingStorage", Map.class);

            Map<Long, TrainingType> trainingTypeStorage =
                    (Map<Long, TrainingType>) context.getBean("trainingTypeStorage", Map.class);

            assertEquals(2, traineeStorage.size());
            assertEquals(2, trainerStorage.size());
            assertEquals(2, trainingStorage.size());
            assertEquals(3, trainingTypeStorage.size());

            assertTrue(traineeStorage.containsKey(1L));
            assertTrue(trainerStorage.containsKey(1L));
            assertTrue(trainingStorage.containsKey(1L));
            assertTrue(trainingTypeStorage.containsKey(1L));
        }
    }

    @Test
    void shouldCreateDaoBeans() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(AppConfig.class)) {

            assertNotNull(context.getBean(TraineeDao.class));
            assertNotNull(context.getBean(TrainerDao.class));
            assertNotNull(context.getBean(TrainingDao.class));
        }
    }

    @Test
    void shouldCreateServiceBeans() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(AppConfig.class)) {

            assertNotNull(context.getBean(TraineeService.class));
            assertNotNull(context.getBean(TrainerService.class));
            assertNotNull(context.getBean(TrainingService.class));
        }
    }

    @Test
    void shouldCreateFacadeBean() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(AppConfig.class)) {

            GymFacade gymFacade = context.getBean(GymFacade.class);

            assertNotNull(gymFacade);
        }
    }
}