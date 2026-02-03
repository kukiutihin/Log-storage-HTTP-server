package server.models;

import java.util.Optional;

public enum Method {
    GET, HEAD, PUT, POST, PATCH, TRACE, OPTIONS, DELETE;

    static public Optional<Method> fromString(String method) {
        try {
            return Optional.of(valueOf(method.toUpperCase()));

        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
