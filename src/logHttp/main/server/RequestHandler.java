package server;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.time.LocalDateTime;

import server.models.Request;
import server.models.Response;
import server.models.Status;

public class RequestHandler {
    static public Optional<Response> handle(Socket socket) {
        try {
            Optional<Request> request = RequestBuilder.buildRequest(
                new BufferedReader(new InputStreamReader(socket.getInputStream()))
            );

            if (request.isEmpty()) return Optional.empty();
            
            String resp = "Privet, ya tut, vot vremya: " + LocalDateTime.now();
            Map<String, String> headers = new HashMap<>();
            return Optional.of(new Response(Status.OK, 1.1, headers, Optional.of(resp)));

        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
