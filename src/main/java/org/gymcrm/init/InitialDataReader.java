package org.gymcrm.init;

import org.gymcrm.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class InitialDataReader {
    private static final Logger logger = LoggerFactory.getLogger(InitialDataReader.class);

    public List<String> readLines(Resource initialDataFile) {
        List<String> lines = new ArrayList<>();

        if (initialDataFile == null || !initialDataFile.exists()) {
            throw new ValidationException("Initial data file was not found");
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(initialDataFile.getInputStream(), StandardCharsets.UTF_8))) {

            String line;

            while ((line = reader.readLine()) != null) {
                String trimmedLine = line.trim();

                if (!trimmedLine.isEmpty() && !trimmedLine.startsWith("#")) {
                    lines.add(trimmedLine);
                }
            }

        } catch (Exception e) {
            logger.error("Cannot read initial data file", e);
            throw new ValidationException("Cannot read initial data file", e);
        }

        return lines;
    }
}