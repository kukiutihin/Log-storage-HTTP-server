package server;

import java.io.IOException;
import java.io.OutputStream;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import server.models.Response;
import server.interfaces.IRequestHandler;
import utils.Context;

public class Server {
    private ServerSocket serv;
    private ExecutorService executors;
    private IRequestHandler handler;

    static private AtomicLong GEN = new AtomicLong(0);
    static private Logger LOG = System.getLogger(Server.class.getName());

    public Server(int port, int poolSize, IRequestHandler handler) throws IOException {
        serv = new ServerSocket(port);
        executors = Executors.newFixedThreadPool(poolSize);
        this.handler = handler;
    }

    public void start() {
        while (true) {
            try {
                Socket client;
                client = serv.accept();                
                Context context = new Context(GEN.incrementAndGet());
                LOG.log(Level.INFO, "[{0}] connection accepted", context.userId());
                executors.submit(() -> respond(client, context));

            } catch (IOException e) {
                LOG.log(Level.WARNING, "failed to accept connection: {0}", e.toString());
            }
        }
    }

    private void respond(Socket client, Context context) {
        try (OutputStream out = client.getOutputStream()) {
            Optional<Response> response = handler.handle(client, context);
            if (response.isEmpty()) {
                LOG.log(Level.INFO, "[{0}] closing connection without response", context.userId());
                return;
            }
            
            out.write(response.get().toBytes());
            LOG.log(Level.INFO, "[{0}] response was sent successfully", context.userId());
            
        } catch (Exception e) {
            LOG.log(Level.WARNING, "[{0}] exception while processing request: {1}", 
                context.userId(), e.toString());
        }
    }
}
