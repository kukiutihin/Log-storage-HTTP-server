package database.interfaces;

import java.sql.SQLException;

import server.models.Log;

public interface DatabaseI {
    public void put(Log log) throws SQLException;
}    
