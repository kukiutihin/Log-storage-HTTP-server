package server.processing;

import java.util.Map;
import java.util.Optional;

import common.Log;
import common.Options;

import java.util.List;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.Socket;

import java.sql.SQLException;

import server.interfaces.IJsonHandler;
import server.interfaces.IRequestBuilder;
import server.interfaces.IRequestHandler;
import server.interfaces.IOptionsBuilder;

import server.models.EStatus;
import server.models.Request;
import server.models.Response;
import utils.Context;
import utils.Err;
import utils.ErrorType;
import utils.Ok;
import utils.Result;
import database.interfaces.DatabaseI;

public class RequestHandler implements IRequestHandler {
    IRequestBuilder builder;
    DatabaseI db;
    IJsonHandler json;
    IOptionsBuilder optionsBuilder;

    static private Logger LOG = System.getLogger(RequestHandler.class.getName());

    public RequestHandler(IRequestBuilder builder, DatabaseI db, IJsonHandler json, IOptionsBuilder optionsBuilder) {
        this.builder = builder;
        this.db = db;
        this.json = json;
        this.optionsBuilder = optionsBuilder;
    }
    
    public Optional<Response> handle(Socket socket, Context context) {
        try {
            Result<Request> requestR = builder.build(
                new BufferedReader(new InputStreamReader(socket.getInputStream()))
            );

            if (requestR instanceof Err<?> err) {
                if (err.type() == ErrorType.REAL_ERROR) {
                    LOG.log(Level.WARNING, "[{0}]: {1}", context.userId(), err.what());
                    return badRequest();
                } else return Optional.empty();
            }
            var request = ((Ok<Request>)requestR).value();

            Options options = new Options(
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()
            );

            if (request.query().isPresent()) {
                Result<Options> mayOptions = optionsBuilder.build(request.query().get());
                if (mayOptions instanceof Err<?> err) {
                    LOG.log(Level.WARNING, "[{0}]: {1}", context.userId(), err.what());
                    return badRequest();
                }
                options = ((Ok<Options>)mayOptions).value();
            }
        
            switch (request.method()) {
                case GET:
                    try {
                        List<Log> result = db.get(options, 200);
                        Result<String> bodyR = json.logsToJson(result);
                        if (bodyR instanceof Err<?> err) {
                            LOG.log(Level.ERROR, "[{0}]: {1}", context.userId(), err.what());
                            return serverError();
                        }
                        var body = ((Ok<String>)bodyR).value();

                        LOG.log(Level.INFO, "[{0}] returning OK with body: {1}", context.userId(), body);
                        return ok(body);

                    } catch (SQLException e) {
                        LOG.log(Level.ERROR, "[{0}] SQL exception occurred: {1}", 
                            context.userId(), e.toString()
                        );
                        return serverError();
                    }

                case POST:
                    if (request.body().isEmpty()) {
                        LOG.log(Level.WARNING, "[{0}] empty body received in POST request", context.userId());
                        return badRequest();
                    }

                    Result<Log> logR = json.toLog(request.body().get());
                    if (logR instanceof Err<?> err) {
                        LOG.log(Level.WARNING, "[{0}]: {1}", context.userId(), err.what());
                        return badRequest();
                    }
                    var log = ((Ok<Log>)logR).value();

                    try {
                        db.put(log);
                        LOG.log(Level.INFO, "[{0}] returning Accepted", context.userId());
                        return accepted();

                    } catch (SQLException e) {
                        LOG.log(Level.ERROR, "[{0}] SQL exception occurred: {1}", context.userId(), e.toString());
                        return serverError();     
                    }

                case DELETE:
                    try {
                        db.delete(options);
                        LOG.log(Level.INFO, "[{0}] returning OK", context.userId());
                        return ok();

                    } catch (SQLException e) {
                        LOG.log(Level.ERROR, "[{0}] SQL exception occurred: {1}", context.userId(), e.toString());
                        return serverError();
                    }

                default:
                    LOG.log(Level.ERROR, "[{0}] Not implemented", context.userId());
                    return notImplemented();
            }
        } catch (IOException e) {
            LOG.log(Level.WARNING, "[{0}] IO exception occurred: {1}", context.userId(), e.toString());
            return serverError();
        }
    }

    private Optional<Response> badRequest() {
        return Optional.of(new Response(EStatus.BAD_REQUEST, 1.1, Map.of(), Optional.empty()));
    }

    private Optional<Response> notImplemented() {
        return Optional.of(new Response(EStatus.NOT_IMPLEMENTED, 1.1, Map.of(), Optional.empty()));
    }

    private Optional<Response> accepted() {
        return Optional.of(new Response(EStatus.ACCEPTED, 1.1, Map.of(), Optional.empty()));
    }

    private Optional<Response> serverError() {
        return Optional.of(new Response(EStatus.SERVER_ERROR, 1.1, Map.of(), Optional.empty()));
    }

    private Optional<Response> ok(String body) {
        return Optional.of(new Response(EStatus.OK, 1.1, Map.of(), Optional.of(body)));
    }

    private Optional<Response> ok() {
        return Optional.of(new Response(EStatus.OK, 1.1, Map.of(), Optional.empty()));
    }
}
