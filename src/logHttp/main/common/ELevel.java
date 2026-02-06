package common;

import java.util.Optional;

public enum ELevel {
    ERROR, WARNING, INFO, DEBUG;

    static public Optional<ELevel> fromString(String str) {
        try {
            return Optional.of(valueOf(str.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
