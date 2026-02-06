package common;

import java.sql.Timestamp;

public record Log(
    String service,
    ELevel level,
    String message,
    String trace,
    Timestamp timestamp
) {}
