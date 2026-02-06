package server.models;

import java.util.function.Predicate;

import common.ELevel;
import utils.Err;
import utils.ErrorType;
import utils.Ok;
import utils.Result;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;


public enum EQueryOption {
    SINCE((s) -> dateCheck(s)),
    BEFORE((s) -> dateCheck(s)),

    SERVICE((s) -> {
        return true;
    }),

    LEVEL((s) -> {
        if (ELevel.fromString(s).isPresent()) return true;
        else return false;
    })

    ;

    private Predicate<String> predicate;

    private EQueryOption(Predicate<String> p) {
        predicate = p;
    }

    static private boolean dateCheck(String s) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")
                .withResolverStyle(ResolverStyle.STRICT);

            LocalDateTime.parse(s, formatter);
            return true;

        } catch (DateTimeException e) {
            return false;
        }
    }

    static public Result<EQueryOption> fromString(String str) {
        try {
            return new Ok<>(valueOf(str.toUpperCase()));
        } catch (Exception e) {
            return new Err<>(
                String.format("failed to convert %s in QueryOption", str), 
                ErrorType.REAL_ERROR
            );
        }
    }

    public boolean check(String s) {
        return predicate.test(s);
    }
}
