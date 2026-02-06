package server.interfaces;

import common.Options;
import utils.Result;

public interface IOptionsBuilder {
    public Result<Options> build(String query);
}
