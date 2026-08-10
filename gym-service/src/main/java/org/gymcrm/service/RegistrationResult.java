package org.gymcrm.service;

public record RegistrationResult<T>(T entity, String rawPassword) {}