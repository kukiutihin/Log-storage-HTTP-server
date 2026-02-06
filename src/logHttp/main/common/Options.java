package common;

import java.time.LocalDateTime;
import java.util.Optional;

public record Options(
    Optional<LocalDateTime> since,
    Optional<LocalDateTime> before,
    Optional<String> service,
    Optional<ELevel> level
) {}
