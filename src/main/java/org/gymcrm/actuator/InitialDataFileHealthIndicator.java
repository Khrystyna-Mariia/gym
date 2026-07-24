package org.gymcrm.actuator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class InitialDataFileHealthIndicator implements HealthIndicator {

    @Value("${initial.data.file:classpath:initial-data.txt}")
    private Resource initialDataFile;

    @Override
    public Health health() {
        if (initialDataFile != null && initialDataFile.exists() && initialDataFile.isReadable()) {
            return Health.up()
                    .withDetail("file", initialDataFile.getFilename())
                    .withDetail("status", "Initial data file is present and readable")
                    .build();
        }
        return Health.down()
                .withDetail("file", initialDataFile != null ? initialDataFile.getFilename() : "unknown")
                .withDetail("error", "Initial data file is missing or unreadable")
                .build();
    }
}