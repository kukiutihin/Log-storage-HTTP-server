package utils;

import java.util.Optional;

public sealed interface Result<T> permits Ok, Err {
    public Optional<T> toOptional();
}




