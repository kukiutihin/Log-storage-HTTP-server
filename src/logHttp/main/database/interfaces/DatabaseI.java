package database.interfaces;

import java.sql.SQLException;
import java.util.List;

import common.Log;
import common.Options;

public interface DatabaseI {
    public void put(Log log) throws SQLException;
    public List<Log> get(Options opt, int limit) throws SQLException;
    public void delete(Options opt) throws SQLException;
}    
