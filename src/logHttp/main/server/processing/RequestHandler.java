package server.processing;

import java.util.Map;
import java.util.Optional;
import java.util.List;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.sql.SQLException;
import java.lang.System.Logger.Level;
import java.lang.System.Logger;

import server.ClientContext;
import server.interfaces.RequestBuilderI;
import server.interfaces.RequestHandlerI;
import server.models.Log;
import server.models.Request;
import server.models.Response;
import server.models.Status;

import database.interfaces.DatabaseI;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class RequestHandler implements RequestHandlerI {
    RequestBuilderI builder;
    DatabaseI db;

    static private Logger LOG = System.getLogger(RequestHandler.class.getName());

    public RequestHandler(RequestBuilderI builder, DatabaseI db) {
        this.builder = builder;
        this.db = db;
    }
    
    public Response handle(Socket socket, ClientContext context) {
        try {
            Optional<Request> request = builder.build(
                new BufferedReader(new InputStreamReader(socket.getInputStream()))
            );

            if (request.isEmpty()) {
                return badRequest(context);
            }

            List<String> path = request.get().path();
            if (path.size() > 1 && !(path.get(0)).equals("logs"))
                return new Response(Status.NOT_FOUND, 1.1, Map.of(), Optional.empty());
        
            switch (request.get().method()) {
                case GET:
                    return notImplemented(context);

                case POST:
                    if (request.get().body().isEmpty())
                        return badRequest(context);

                    Optional<Log> log = parseBody(request.get().body().get());
                    if (log.isEmpty())
                        return badRequest(context);

                    try {
                        db.put(log.get());
                        return accepted();

                    } catch (SQLException e) {
                        LOG.log(Level.WARNING, "[{0}] database returned error: {1}\n", context.to36String(), e);
                        return serverError();     
                    }

                case DELETE:
                    return notImplemented(context);

                default:
                    return notImplemented(context);
            }
        } catch (IOException e) {
            return notImplemented(context);
        }
    }

    private Response badRequest(ClientContext context) {
        LOG.log(Level.WARNING, "[{0}] bad request\n", context.to36String());
        return new Response(Status.BAD_REQUEST, 1.1, Map.of(), Optional.empty());
    }

    private Response notImplemented(ClientContext context) {
        LOG.log(Level.WARNING, "[{0}] not implemented\n", context.to36String());
        return new Response(Status.NOT_IMPLEMENTED, 1.1, Map.of(), Optional.empty());
    }

    private Response accepted() {
        return new Response(Status.ACCEPTED, 1.1, Map.of(), Optional.empty());
    }

    private Response serverError() {
        return new Response(Status.SERVER_ERROR, 1.1, Map.of(), Optional.empty());
    }

    private Optional<Log> parseBody(char[] body) {
        try {
            Gson gson = new Gson();
            String bodyStr = new String(body);
            return Optional.of(gson.fromJson(bodyStr, Log.class));

        } catch (JsonSyntaxException e) {
            LOG.log(Level.WARNING, "error while parsing body: {0}\n{1}", e.toString(), body.toString());
            return Optional.empty();
        }
    }
}
