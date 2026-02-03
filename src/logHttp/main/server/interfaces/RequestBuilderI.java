package server.interfaces;

import java.io.BufferedReader;
import java.util.Optional;

import server.models.Request;

public interface RequestBuilderI {
    public Optional<Request> build(BufferedReader reader);
}
