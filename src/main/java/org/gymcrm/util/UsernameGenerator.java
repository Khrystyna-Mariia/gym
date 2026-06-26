package org.gymcrm.util;

import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class UsernameGenerator {

    public String generate(String firstName, String lastName, Collection<String> existingUsernames) {
        if (firstName == null || lastName == null) {
            throw new IllegalArgumentException("First name and last name must not be null");
        }

        String baseUsername = firstName.trim() + "." + lastName.trim();

        if (!existingUsernames.contains(baseUsername)) {
            return baseUsername;
        }

        int serialNumber = 1;
        String usernameWithSuffix = baseUsername + serialNumber;

        while (existingUsernames.contains(usernameWithSuffix)) {
            serialNumber++;
            usernameWithSuffix = baseUsername + serialNumber;
        }

        return usernameWithSuffix;
    }
}