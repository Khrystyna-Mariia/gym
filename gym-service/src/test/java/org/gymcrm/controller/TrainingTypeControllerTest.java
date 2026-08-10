package org.gymcrm.controller;

import org.gymcrm.dto.response.TrainingTypeResponse;
import org.gymcrm.mapper.TrainingMapper;
import org.gymcrm.model.TrainingType;
import org.gymcrm.model.TrainingTypeEnum;
import org.gymcrm.service.TrainingTypeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TrainingTypeControllerTest {

    @Mock
    private TrainingTypeService trainingTypeService;

    @Mock
    private TrainingMapper trainingMapper;

    @InjectMocks
    private TrainingTypeController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getTrainingTypes_returnsAllTypesWithOkStatus() throws Exception {
        TrainingType yoga = new TrainingType(1L, TrainingTypeEnum.YOGA);
        List<TrainingType> entities = List.of(yoga);
        List<TrainingTypeResponse> response = List.of(new TrainingTypeResponse("YOGA", 1L));

        when(trainingTypeService.selectAll()).thenReturn(entities);
        when(trainingMapper.toTrainingTypeResponseList(entities)).thenReturn(response);

        mockMvc.perform(get("/api/v1/training-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trainingType").value("YOGA"))
                .andExpect(jsonPath("$[0].trainingTypeId").value(1));

        verify(trainingTypeService).selectAll();
    }

    @Test
    void getTrainingTypes_returnsEmptyListWhenNoneExist() throws Exception {
        when(trainingTypeService.selectAll()).thenReturn(List.of());
        when(trainingMapper.toTrainingTypeResponseList(List.of())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/training-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}