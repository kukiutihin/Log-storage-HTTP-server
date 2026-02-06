import server.Server;
import server.interfaces.IJsonHandler;
import server.interfaces.IOptionsBuilder;
import server.interfaces.IRequestBuilder;
import server.interfaces.IRequestHandler;
import server.processing.OptionsBuilder;
import server.processing.JsonHandler;
import server.processing.RequestBuilder;
import server.processing.RequestHandler;

import database.Database;
import database.interfaces.DatabaseI;

import java.io.FileInputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Properties;


public class App {
    static private Logger LOG = System.getLogger(App.class.getName());
    public static void main(String[] args) {
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            Properties config = new Properties();
            config.load(fis);

            String url = config.getProperty("db.url");
            String user = config.getProperty("db.user");
            String password = config.getProperty("db.password");
            DatabaseI database = new Database(url, user, password);

            LOG.log(Level.INFO, "database connected");

            IRequestBuilder builder = new RequestBuilder();
            IOptionsBuilder optionsBuilder = new OptionsBuilder();
            IJsonHandler jsonHandler = new JsonHandler();

            IRequestHandler handler = new RequestHandler(builder, database, jsonHandler, optionsBuilder);

            Server server = new Server(1337, 8, handler);
            LOG.log(Level.INFO, "server starting");
            server.start();

        } catch (Exception e) {
            LOG.log(Level.ERROR, "server crashed: {0}", e.toString());
        }
    }
}