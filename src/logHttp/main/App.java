import server.Server;
import server.interfaces.RequestBuilderI;
import server.interfaces.RequestHandlerI;
import server.processing.RequestBuilder;
import server.processing.RequestHandler;

import java.io.FileInputStream;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Properties;

import database.Database;
import database.interfaces.DatabaseI;

public class App {
    static Logger LOG = System.getLogger(App.class.getName());
    public static void main(String[] args) {
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            Properties config = new Properties();
            config.load(fis);

            String url = config.getProperty("db.url");
            String user = config.getProperty("db.user");
            String password = config.getProperty("db.password");
            DatabaseI database = new Database(url, user, password);

            RequestBuilderI builder = new RequestBuilder();
            RequestHandlerI handler = new RequestHandler(builder, database);

            Server server = new Server(1337, 8, handler);
            server.start();

        } catch (Exception e) {
            String trace = "";
            for (var s : e.getStackTrace()) {
                trace += s.toString() + "\n";
            }
            LOG.log(Level.ERROR, "Server crashed: {0}\nTrace: {1}", e.toString(), trace);
        }
    }
}