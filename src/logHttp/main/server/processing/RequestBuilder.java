package server.processing;

import server.models.Request;
import server.models.EMethod;
import server.interfaces.IRequestBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import utils.Result;
import utils.Err;
import utils.ErrorType;
import utils.Ok;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;;


public class RequestBuilder implements IRequestBuilder {

    static private Logger LOG = System.getLogger(RequestBuilder.class.getName());
    
    @Override
    public Result<Request> build(BufferedReader reader) {
        try {
            String rawFirst = reader.readLine();
            LOG.log(Level.INFO, "rawFirst: {0}", rawFirst);

            if (rawFirst == null)
                return new Err<>("", ErrorType.VALUE_MISSING);

            if (rawFirst.isBlank())
                return new Err<>("Empty request received", ErrorType.REAL_ERROR);

            Result<EMethod> methodR = parseMethod(rawFirst);
            if (methodR instanceof Err<?> err) 
                return new Err<>(err.what(), ErrorType.REAL_ERROR);
            var method = ((Ok<EMethod>)methodR).value();
            LOG.log(Level.INFO, "method: {0}", method);

            Result<List<String>> pathR = parsePath(rawFirst);
            if (pathR instanceof Err<?> err) 
                return new Err<>(err.what(), ErrorType.REAL_ERROR);
            var path = ((Ok<List<String>>)pathR).value();

            String pathDebug = "";
            for (String p : path) pathDebug += p += "/";
            LOG.log(Level.INFO, "path: {0}", pathDebug);

            Result<String> queryR = parseQuery(rawFirst);
            if (queryR instanceof Err<?> err && err.type() == ErrorType.REAL_ERROR)
                return new Err<>(err.what(), ErrorType.REAL_ERROR);
            var query = queryR.toOptional();

            if (query.isPresent())
                LOG.log(Level.INFO, "query: {0}", query);

            Result<Double> versionR = parseVersion(rawFirst);
            if (versionR instanceof Err<?> err) 
                return new Err<>(err.what(), ErrorType.REAL_ERROR);
            var version = ((Ok<Double>)versionR).value();
            LOG.log(Level.INFO, "version: {0}", version);

            Result<Map<String, String>> headersR = parseHeaders(reader);
            if (headersR instanceof Err<?> err) 
                return new Err<>(err.what(), ErrorType.REAL_ERROR);
            var headers = ((Ok<Map<String, String>>)headersR).value();

            Result<char[]> bodyR = parseBody(headers, reader);
            if (bodyR instanceof Err<?> err && err.type() == ErrorType.REAL_ERROR)
                return new Err<>(err.what(), ErrorType.REAL_ERROR);
            var body = bodyR.toOptional();

            if (body.isPresent()) {
                LOG.log(Level.INFO, "body: {0}", new String(body.get()));
            }

            return new Ok<>(new Request(
                method, path, version, headers, query, body
            ));

        } catch (IOException e) {
            return new Err<>("An error occurred: " + e.toString(), ErrorType.REAL_ERROR);
        }
    }

    private Result<EMethod> parseMethod(String raw) {
        String method = raw.trim().split(" ")[0];
        return EMethod.fromString(method.trim());
    }

    private Result<List<String>> parsePath(String raw) {
        List<String> rawChunks = Arrays.stream(raw.split(" "))
        .filter((s) -> !s.isEmpty())
        .toList();
        
        if (rawChunks.size() < 2)
            return new Err<>("Failed to parse path: " + raw, ErrorType.REAL_ERROR);
        
        try {
            URI fullPath = new URI(rawChunks.get(1));
            return new Ok<>(Arrays.stream(fullPath.getPath()
                .split("/"))
                .filter((s) -> !s.isEmpty())
                .toList());

        } catch (URISyntaxException e) {
            return new Err<>("An error occurred: " + e.toString(), ErrorType.REAL_ERROR);
        }
    }

    private Result<String> parseQuery(String raw) {
        List<String> rawChunks = Arrays.stream(raw.split(" "))
        .filter((s) -> !s.isEmpty())
        .toList();

        if (rawChunks.size() < 2) 
            return new Err<>("Failed to parse query: " + raw, ErrorType.REAL_ERROR);
        
        try {
            URI fullPath = new URI(rawChunks.get(1));
            String query = fullPath.getQuery();
            if (query == null)
                return new Err<>("", ErrorType.VALUE_MISSING);
            
            return new Ok<>(query);

        } catch (URISyntaxException e) {
            return new Err<>("An error occurred: " + e.toString(), ErrorType.REAL_ERROR);
        }
    }

    private Result<Double> parseVersion(String raw) {
        List<String> rawChunks = Arrays.stream(raw.split(" "))
                .filter((s) -> !s.isEmpty())
                .toList();

        if (rawChunks.size() < 3) 
            return new Err<>("Failed to parse version: " + raw, ErrorType.REAL_ERROR);
 
        List<String> rawVersion = Arrays.stream(rawChunks.get(2).split("/"))
            .filter((s) -> !s.isEmpty())
            .toList();

        if (rawVersion.size() < 2)
            return new Err<>("Failed to parse version: " + raw, ErrorType.REAL_ERROR);

        try {
            return new Ok<>(Double.parseDouble(rawVersion.get(1)));

        } catch (NumberFormatException e) {
            return new Err<>("An error occurred: " + e.toString(), ErrorType.REAL_ERROR);
        }
    }

    private Result<Map<String, String>> parseHeaders(BufferedReader reader) {
        try {
            Map<String, String> headers = new HashMap<>();
            String header;
            while (!(header = reader.readLine()).isEmpty()) {
                List<String> pair = Arrays.stream(header.split(":"))
                    .map((s) -> s.trim())
                    .filter((s) -> !s.isEmpty())
                    .toList();

                if (pair.size() < 2) 
                    return new Err<>("Incorrect header: " + header, ErrorType.REAL_ERROR);
                headers.put(pair.get(0), pair.get(1));
            }

            return new Ok<>(headers);

        } catch (IOException e) {
            return new Err<>("An error occurred: " + e.toString(), ErrorType.REAL_ERROR);
        }
    }
    
    private Result<char[]> parseBody(Map<String, String> headers, BufferedReader reader) {
        try {
            if (headers.containsKey("Content-Length")) {
                int bytes = Integer.parseInt(headers.get("Content-Length"));
                
                if (bytes <= 0) return new Err<>("Body is missing", ErrorType.REAL_ERROR);
                else {
                    char[] buf = new char[bytes];
                    int readBytes = reader.read(buf);
                    
                    if (readBytes < bytes) 
                        return new Err<>("Body is smaller than stated", ErrorType.REAL_ERROR);

                    return new Ok<>(buf);
                }
            } else return new Err<>("Content-Length is missing", ErrorType.VALUE_MISSING);

        } catch (IOException e) {
            return new Err<>("An error occurred: " + e.toString(), ErrorType.REAL_ERROR);
        }
    }
}
