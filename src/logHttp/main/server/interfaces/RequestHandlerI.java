package server.interfaces;

import java.net.Socket;

import server.ClientContext;
import server.models.Response;

public interface RequestHandlerI {
    Response handle(Socket client, ClientContext context);
}
