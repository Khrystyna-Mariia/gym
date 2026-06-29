package org.gymcrm.util;

import org.springframework.stereotype.Component;

import java.util.function.Predicate;

@Component
public class UsernameGenerator {

    public String generate(String firstName, String lastName, Predicate<String> usernameExists) {
        if (firstName == null || lastName == null) {
            throw new IllegalArgumentException("First name and last name must not be null");
        }

        String baseUsername = firstName.trim() + "." + lastName.trim();

        if (!usernameExists.test(baseUsername)) {
            return baseUsername;
        }

        int serialNumber = 1;
        String usernameWithSuffix = baseUsername + serialNumber;

        while (usernameExists.test(usernameWithSuffix)) {
            serialNumber++;
            usernameWithSuffix = baseUsername + serialNumber;
        }

        return usernameWithSuffix;
    }
}