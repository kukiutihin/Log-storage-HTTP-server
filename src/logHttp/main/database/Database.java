package database;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import java.sql.SQLException;
import java.sql.PreparedStatement;

import server.models.Log;
import database.interfaces.DatabaseI;

public class Database implements DatabaseI {
    private DatabaseHandler handler;

    static private Logger LOG = System.getLogger(Database.class.getName());

    public Database(String url, String user, String password) throws SQLException {
        handler = new DatabaseHandler(url, user, password);
        LOG.log(Level.INFO, "Database connected");
    }

    @Override
    public void put(Log log) throws SQLException {
        String sql = "insert into logs (service, level, message, trace) values (?, ?, ?, ?)";
        PreparedStatement statement = handler.getConnection().prepareStatement(sql);
        statement.setString(1, log.service());
        statement.setString(2, log.level());
        statement.setString(3, log.message());
        statement.setString(4, log.traceId());
        statement.execute();
    }
}
