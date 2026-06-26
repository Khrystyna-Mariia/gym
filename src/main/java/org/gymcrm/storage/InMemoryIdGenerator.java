package org.gymcrm.storage;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

@Component
public class InMemoryIdGenerator {

    public <T> Long generateNextId(Collection<T> items, Function<T, Long> idExtractor) {
        if (items == null) {
            throw new IllegalArgumentException("Items collection must not be null");
        }

        if (idExtractor == null) {
            throw new IllegalArgumentException("Id extractor must not be null");
        }

        return items.stream()
                .map(idExtractor)
                .filter(Objects::nonNull)
                .max(Long::compareTo)
                .orElse(0L) + 1;
    }
}