package server.models;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.Map;

public enum Status {
    OK(Series.SUCCESS, "OK", 200),
    CREATED(Series.SUCCESS, "Created", 201),
    ACCEPTED(Series.SUCCESS, "Accepted", 202),

    BAD_REQUEST(Series.CLIENT_ERROR, "Bad request", 400),
    NOT_FOUND(Series.CLIENT_ERROR, "Not found", 404),
    TOO_LARGE(Series.CLIENT_ERROR, "Payload too large", 413)

    ;

    private final Series series;
    private final int code;
    private final String description;

    private static final Map<Integer, Status> codeMap =
        Arrays.stream(values())
            .collect(Collectors.toMap((s) -> s.code, (s) -> s));

    private Status(Series series, String description, int code) {
        this.series = series;
        this.description = description;
        this.code = code;
    }

    public Series getSeries() { return series; }
    public int getCode() { return code; }
    public String getDescription() { return description; }

    public boolean isOK() {
        return series != Series.CLIENT_ERROR && series != Series.SERVER_ERROR;
    }

    static public Optional<Status> fromCode(int code) {
        return Optional.ofNullable(codeMap.get(code));
    }

    enum Series {
        INFORMATIONAL, SUCCESS, REDIRECTIONAL, CLIENT_ERROR, SERVER_ERROR;
    }
}
