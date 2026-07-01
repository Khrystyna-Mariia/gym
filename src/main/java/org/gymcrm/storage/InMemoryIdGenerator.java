package org.gymcrm.storage;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class InMemoryIdGenerator {

    private final AtomicLong traineeCounter = new AtomicLong(0);
    private final AtomicLong trainerCounter = new AtomicLong(0);
    private final AtomicLong trainingCounter = new AtomicLong(0);

    public Long generateNextTraineeId() {
        return traineeCounter.incrementAndGet();
    }

    public Long generateNextTrainerId() {
        return trainerCounter.incrementAndGet();
    }

    public Long generateNextTrainingId() {
        return trainingCounter.incrementAndGet();
    }

    public void initializeMaxTraineeId(Long maxId) {
        updateCounterIfGreater(traineeCounter, maxId);
    }

    public void initializeMaxTrainerId(Long maxId) {
        updateCounterIfGreater(trainerCounter, maxId);
    }

    public void initializeMaxTrainingId(Long maxId) {
        updateCounterIfGreater(trainingCounter, maxId);
    }

    private void updateCounterIfGreater(AtomicLong counter, Long value) {
        if (value != null) {
            counter.updateAndGet(current -> Math.max(current, value));
        }
    }
}