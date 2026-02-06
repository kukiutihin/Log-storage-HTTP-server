package server.models;

import java.util.stream.Collectors;

import utils.Err;
import utils.ErrorType;
import utils.Ok;
import utils.Result;

import java.util.Arrays;
import java.util.Map;

public enum EStatus {
    OK(Series.SUCCESS, "OK", 200),
    ACCEPTED(Series.SUCCESS, "Accepted", 202),

    BAD_REQUEST(Series.CLIENT_ERROR, "Bad request", 400),
    NOT_FOUND(Series.CLIENT_ERROR, "Not found", 404),
    TOO_LARGE(Series.CLIENT_ERROR, "Payload too large", 413),

    SERVER_ERROR(Series.SERVER_ERROR, "What`s going on", 500),
    NOT_IMPLEMENTED(Series.SERVER_ERROR, "Sorry, not implemented", 501)

    ;

    private final Series series;
    private final int code;
    private final String description;

    private static final Map<Integer, EStatus> codeMap =
        Arrays.stream(values())
            .collect(Collectors.toMap((s) -> s.code, (s) -> s));

    private EStatus(Series series, String description, int code) {
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

    static public Result<EStatus> fromCode(int code) {
       if (codeMap.containsKey(code)) return new Ok<>(codeMap.get(code));
       else return new Err<>("Failed to create Status from code: " + code, ErrorType.REAL_ERROR);
    }

    enum Series {
        INFORMATIONAL, SUCCESS, REDIRECTIONAL, CLIENT_ERROR, SERVER_ERROR;
    }
}
