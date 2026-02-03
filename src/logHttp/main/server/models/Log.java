package server.models;

public record Log(
    String service,
    String level,
    String message,
    String traceId
) {}
