package server.processing;

import java.util.List;

import common.Log;
import server.interfaces.IJsonHandler;

import utils.Err;
import utils.ErrorType;
import utils.Ok;
import utils.Result;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class JsonHandler implements IJsonHandler {
    Gson gson;

    public JsonHandler() {
        gson = new Gson();
    }

    public Result<Log> toLog(char[] body) {
        String bodyStr = new String(body);
        try {
            Log result = gson.fromJson(bodyStr, Log.class);
            if (result.level() == null || result.message() == null || result.service() == null || result.trace() == null)
                return new Err<>("Failed to parse log: " + bodyStr, ErrorType.REAL_ERROR);

            return new Ok<Log>(result);
        } catch (JsonSyntaxException e) {
            return new Err<Log>("Json parsing error: " + e.toString(), ErrorType.REAL_ERROR);
        }
    }

    public Result<String> logsToJson(List<Log> logs) {
        try {
            return new Ok<>(gson.toJson(logs));
        } catch (Exception e) {
            return new Err<>("Failed to serialize to Json: " + e.toString(), ErrorType.REAL_ERROR);
        }
    }
}
