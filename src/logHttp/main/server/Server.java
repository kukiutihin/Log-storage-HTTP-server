package server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import server.models.Response;
import server.interfaces.RequestHandlerI;

public class Server {
    private ServerSocket serv;
    private ExecutorService executors;
    private RequestHandlerI handler;

    static private Logger LOG = System.getLogger(Server.class.getName());
    static private AtomicLong GEN = new AtomicLong();

    public Server(int port, int poolSize, RequestHandlerI handler) throws IOException {
        serv = new ServerSocket(port);
        executors = Executors.newFixedThreadPool(poolSize);
        this.handler = handler;
    }

    public void start() {
        while (true) {
            try {
                Socket client;
                client = serv.accept();

                long id = GEN.incrementAndGet();
                ClientContext context = new ClientContext(id);
                
                LOG.log(Level.INFO, "[{0}] connection accepted\n", context.to36String());
                executors.submit(() -> respond(client, context));

            } catch (IOException e) {
                LOG.log(Level.WARNING, "Failed to accept connection: {0}\n", e);
            }
        }
    }

    private void respond(Socket client, ClientContext context) {
        try (OutputStream out = client.getOutputStream()) {
            Response response = handler.handle(client, context);
            out.write(response.toBytes());
            LOG.log(Level.INFO, "[{0}] success\n", context.to36String());
            
        } catch (IOException e) {
            LOG.log(Level.WARNING, "[{0}] client cancelled the connection: {1}\n", 
                context.to36String(), e.toString());
        }
    }
}
