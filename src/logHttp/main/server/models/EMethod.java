package server.models;

import utils.Result;
import utils.Ok;
import utils.Err;
import utils.ErrorType;

public enum EMethod {
    GET, HEAD, PUT, POST, PATCH, TRACE, OPTIONS, DELETE;

    static public Result<EMethod> fromString(String method) {
        try {
            return new Ok<>(valueOf(method.toUpperCase()));

        } catch (IllegalArgumentException e) {
            return new Err<>("Failed to parse method from: " + method, ErrorType.REAL_ERROR);
        }
    }
}
