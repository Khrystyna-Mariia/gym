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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(AppConfig.class)
class AppConfigTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private TraineeDao traineeDao;

    @Autowired
    private TrainerDao trainerDao;

    @Autowired
    private TrainingDao trainingDao;

    @Autowired
    private TraineeService traineeService;

    @Autowired
    private TrainerService trainerService;

    @Autowired
    private TrainingService trainingService;

    @Autowired
    private GymFacade gymFacade;

    @Autowired
    @Qualifier("traineeStorage")
    private Map<Long, Trainee> traineeStorage;

    @Autowired
    @Qualifier("trainerStorage")
    private Map<Long, Trainer> trainerStorage;

    @Autowired
    @Qualifier("trainingStorage")
    private Map<Long, Training> trainingStorage;

    @Autowired
    @Qualifier("trainingTypeStorage")
    private Map<Long, TrainingType> trainingTypeStorage;

    @Test
    void shouldStartSpringContextSuccessfully() {
        assertNotNull(context);
    }

    @Test
    void shouldInitializeStorageWithDataFromFile() {
        assertEquals(2, traineeStorage.size());
        assertEquals(2, trainerStorage.size());
        assertEquals(2, trainingStorage.size());
        assertEquals(3, trainingTypeStorage.size());

        assertTrue(traineeStorage.containsKey(1L));
        assertTrue(trainerStorage.containsKey(1L));
        assertTrue(trainingStorage.containsKey(1L));
        assertTrue(trainingTypeStorage.containsKey(1L));
    }

    @Test
    void shouldCreateDaoBeans() {
        assertNotNull(traineeDao);
        assertNotNull(trainerDao);
        assertNotNull(trainingDao);
    }

    @Test
    void shouldCreateServiceBeans() {
        assertNotNull(traineeService);
        assertNotNull(trainerService);
        assertNotNull(trainingService);
    }

    @Test
    void shouldCreateFacadeBean() {
        assertNotNull(gymFacade);
    }
}