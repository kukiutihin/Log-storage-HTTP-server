package server.interfaces;

import java.net.Socket;
import java.util.Optional;

import server.models.Response;
import utils.Context;

public interface IRequestHandler {
    Optional<Response> handle(Socket client, Context context);
}
