package utils;

import java.util.Optional;

public record Ok<T>(T value) implements Result<T> {
    @Override
    public Optional<T> toOptional() {
        return Optional.of(value);
    }
}
