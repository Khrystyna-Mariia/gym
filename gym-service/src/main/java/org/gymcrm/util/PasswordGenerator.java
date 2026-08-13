package org.gymcrm.util;

import org.gymcrm.exception.ValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class PasswordGenerator {
    private static final String CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private final SecureRandom random = new SecureRandom();
    private final int passwordLength;

    public PasswordGenerator(@Value("${security.password.length:10}") int passwordLength) {
        if (passwordLength <= 0) {
            throw new ValidationException("Password length must be greater than 0");
        }

        this.passwordLength = passwordLength;
    }

    public String generate() {
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < passwordLength; i++) {
            int index = random.nextInt(CHARACTERS.length());
            password.append(CHARACTERS.charAt(index));
        }

        return password.toString();
    }
}