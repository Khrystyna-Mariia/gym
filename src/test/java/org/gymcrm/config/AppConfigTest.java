package org.gymcrm.config;

import org.gymcrm.dao.TraineeDao;
import org.gymcrm.dao.TrainerDao;
import org.gymcrm.dao.TrainingDao;
import org.gymcrm.facade.GymFacade;
import org.gymcrm.init.InitialDataParser;
import org.gymcrm.service.TraineeService;
import org.gymcrm.service.TrainerService;
import org.gymcrm.service.TrainingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

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
    private InitialDataParser initialDataParser;

    @Test
    void shouldStartSpringContextSuccessfully() {
        assertNotNull(context);
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
    void shouldCreateFacadeAndParserBeans() {
        assertNotNull(gymFacade);
        assertNotNull(initialDataParser);
    }
}