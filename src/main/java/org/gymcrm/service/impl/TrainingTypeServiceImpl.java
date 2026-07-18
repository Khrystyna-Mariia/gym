package org.gymcrm.service.impl;

import org.gymcrm.annotation.RequireAuth;
import org.gymcrm.dao.TrainingTypeDao;
import org.gymcrm.model.TrainingType;
import org.gymcrm.service.TrainingTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class TrainingTypeServiceImpl implements TrainingTypeService {

    private final TrainingTypeDao trainingTypeDao;

    public TrainingTypeServiceImpl(TrainingTypeDao trainingTypeDao) {
        this.trainingTypeDao = trainingTypeDao;
    }

    @Override
    @RequireAuth
    public List<TrainingType> selectAll() {
        return trainingTypeDao.findAll();
    }

    @Override
    public Optional<TrainingType> selectById(Long id) {
        return trainingTypeDao.findById(id);
    }
}