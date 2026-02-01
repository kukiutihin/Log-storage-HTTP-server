package server.models;

import java.util.Map;
import java.util.Optional;

public record Response(
    Status status, 
    double version, 
    Map<String, String> headers, 
    Optional<String> body
) {
    @Override
    public String toString() {
        String response = "HTTP/" + version + 
            " " + status.getCode() + " " + status.getDescription() + "\r\n";
        
        for (String x : headers.keySet())
            response += (x + ": " + headers.get(x) + "\r\n");

        response += "\r\n";
        if (body.isPresent()) response += body.get();

        return response;
    }

    public byte[] toBytes() {
        return toString().getBytes();
    }
}
