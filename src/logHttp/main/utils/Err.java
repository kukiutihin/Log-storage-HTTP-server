package utils;

import java.util.Optional;

public record Err<T>(String what, ErrorType type) implements Result<T> {
    
    @Override
    public Optional<T> toOptional() {
        return Optional.empty();
    }
}

