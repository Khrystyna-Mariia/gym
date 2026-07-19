package org.gymcrm.service.impl;

import org.gymcrm.dao.TrainingTypeDao;
import org.gymcrm.model.TrainingType;
import org.gymcrm.model.TrainingTypeEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingTypeServiceImplTest {

    @Mock
    private TrainingTypeDao trainingTypeDao;

    @InjectMocks
    private TrainingTypeServiceImpl trainingTypeService;

    @Test
    void shouldSelectAllTrainingTypes() {
        List<TrainingType> expectedTypes = List.of(
                new TrainingType(1L, TrainingTypeEnum.FITNESS),
                new TrainingType(2L, TrainingTypeEnum.YOGA)
        );
        when(trainingTypeDao.findAll()).thenReturn(expectedTypes);

        List<TrainingType> result = trainingTypeService.selectAll();

        assertEquals(2, result.size());
        verify(trainingTypeDao).findAll();
    }

    @Test
    void shouldSelectTrainingTypeById() {
        TrainingType expectedType = new TrainingType(1L, TrainingTypeEnum.FITNESS);
        when(trainingTypeDao.findById(1L)).thenReturn(Optional.of(expectedType));

        Optional<TrainingType> result = trainingTypeService.selectById(1L);

        assertTrue(result.isPresent());
        assertEquals(expectedType, result.get());
        verify(trainingTypeDao).findById(1L);
    }
}