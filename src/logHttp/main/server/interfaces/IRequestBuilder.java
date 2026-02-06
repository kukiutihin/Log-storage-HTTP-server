package server.interfaces;

import java.io.BufferedReader;

import server.models.Request;
import utils.Result;

public interface IRequestBuilder {
    public Result<Request> build(BufferedReader reader);
}
