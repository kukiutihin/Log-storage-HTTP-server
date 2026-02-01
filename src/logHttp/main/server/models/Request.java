package server.models;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record Request(
    Method method,
    List<String> path,
    double version,
    Map<String, String> headers,

    Optional<String> query,
    Optional<char[]> body
) {
    @Override
    public List<String> path() {
        return Collections.unmodifiableList(path);
    }

    @Override
    public Map<String, String> headers() {
        return Collections.unmodifiableMap(headers);
    }
}