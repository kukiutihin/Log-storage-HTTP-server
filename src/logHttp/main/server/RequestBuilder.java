package server;

import server.models.Request;
import server.models.Method;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


public class RequestBuilder {
    private static final System.Logger LOG = 
        System.getLogger(RequestBuilder.class.getName());

    static public Optional<Request> buildRequest(BufferedReader reader) {
        try {
            String rawFirst = reader.readLine();
            if (rawFirst == null || rawFirst.isBlank())
                return Optional.empty();

            Optional<Method> method = parseMethod(rawFirst);
            if (method.isEmpty()) return Optional.empty();

            Optional<List<String>> path = parsePath(rawFirst);
            if (path.isEmpty()) return Optional.empty();

            Optional<String> query = parseQuery(rawFirst);
            Optional<Double> version = parseVersion(rawFirst);
            if (version.isEmpty()) return Optional.empty();

            Optional<Map<String, String>> headers = parseHeaders(reader);
            if (headers.isEmpty()) return Optional.empty();

            Optional<char[]> body = parseBody(headers.get(), reader);

            return Optional.of(new Request(
                method.get(),
                path.get(),
                version.get(),
                headers.get(),
                query,
                body
            ));

        } catch (IOException e) {
            return Optional.empty();
        }
    }

    static public Optional<Method> parseMethod(String raw) {
        String method = raw.trim().split(" ")[0];
        return Method.fromString(method.trim());
    }

    static public Optional<List<String>> parsePath(String raw) {
        List<String> rawChunks = Arrays.stream(raw.split(" "))
        .filter((s) -> !s.isEmpty())
        .toList();
        
        if (rawChunks.size() < 2) return Optional.empty();
        
        try {
            URI fullPath = new URI(rawChunks.get(1));
            return Optional.of(Arrays.stream(fullPath.getPath()
                .split("/"))
                .filter((s) -> !s.isEmpty())
                .toList());

        } catch (URISyntaxException e) {
            return Optional.empty();
        }
    }

    static public Optional<String> parseQuery(String raw) {
        List<String> rawChunks = Arrays.stream(raw.split(" "))
        .filter((s) -> !s.isEmpty())
        .toList();

        if (rawChunks.size() < 2) return Optional.empty();
        
        try {
            URI fullPath = new URI(rawChunks.get(1));
            String query = fullPath.getQuery();
            return Optional.ofNullable(query);

        } catch (URISyntaxException e) {
            return Optional.empty();
        }
    }

    static public Optional<Double> parseVersion(String raw) {
        List<String> rawChunks = Arrays.stream(raw.split(" "))
                .filter((s) -> !s.isEmpty())
                .toList();

        if (rawChunks.size() < 3) return Optional.empty();

        List<String> rawVersion = Arrays.stream(rawChunks.get(2).split("/"))
            .filter((s) -> !s.isEmpty())
            .toList();

        if (rawVersion.size() < 2) return Optional.empty();

        try {
            return Optional.of(Double.parseDouble(rawVersion.get(1)));

        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    static public Optional<Map<String, String>> parseHeaders(BufferedReader reader) {
        try {
            Map<String, String> headers = new HashMap<>();
            String header;
            while (!(header = reader.readLine()).isEmpty()) {
                List<String> pair = Arrays.stream(header.split(":"))
                    .map((s) -> s.trim())
                    .filter((s) -> !s.isEmpty())
                    .toList();

                if (pair.size() < 2) return Optional.empty();
                headers.put(pair.get(0), pair.get(1));
            }

            return Optional.of(headers);

        } catch (IOException e) {
            return Optional.empty();
        }
    }
    
    static public Optional<char[]> parseBody(Map<String, String> headers, BufferedReader reader) {
        try {
            if (headers.containsKey("Content-Length")) {
                int bytes = Integer.parseInt(headers.get("Content-Length"));
                
                if (bytes <= 0) return Optional.empty();
                else {
                    char[] buf = new char[bytes];
                    int readBytes = reader.read(buf);
                    
                    if (readBytes < bytes) return Optional.empty();
                    return Optional.of(buf);
                }
            } else return Optional.empty();

        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
