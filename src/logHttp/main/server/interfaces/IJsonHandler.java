package server.interfaces;
import common.Log;
import utils.Result;

import java.util.List;

public interface IJsonHandler {
    public Result<Log> toLog(char[] body);
    public Result<String> logsToJson(List<Log> logs);
}
