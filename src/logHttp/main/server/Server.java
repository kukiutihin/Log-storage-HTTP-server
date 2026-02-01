package server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import server.models.Response;

public class Server {
    int port;
    ServerSocket serv;
    ExecutorService executors;

    public Server(int port, int poolSize) throws IOException {
        this.port = port;
        serv = new ServerSocket(port);
        executors = Executors.newFixedThreadPool(poolSize);
    }

    public void start() {
        while (true) {
            try {
                Socket client = serv.accept();
                executors.submit(() -> respond(client));

            } catch (Exception e) {}
        }
    }

    public void respond(Socket client) {
        Optional<Response> response = RequestHandler.handle(client);
        if (response.isPresent()) {
            try (OutputStream out = client.getOutputStream()) {
                out.write(response.get().toBytes());
            } catch (IOException e) {}
        }
    }
}
