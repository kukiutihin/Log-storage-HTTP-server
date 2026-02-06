package database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import common.ELevel;
import common.Log;
import common.Options;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import database.interfaces.DatabaseI;

public class Database implements DatabaseI {
    private DatabaseHandler handler;

    public Database(String url, String user, String password) throws SQLException {
        handler = new DatabaseHandler(url, user, password);
    }

    @Override
    public void put(Log log) throws SQLException {
        String sql = "insert into logs (service, level, message, trace) values (?, ?, ?, ?)";
        try (PreparedStatement statement = handler.getConnection().prepareStatement(sql)) {
            statement.setString(1, log.service());
            statement.setString(2, log.level().toString());
            statement.setString(3, log.message());
            statement.setString(4, log.trace());
            statement.execute();

        } catch (SQLException e) {
            throw e;
        }
    }

    @Override
    public void delete(Options opt) throws SQLException {
        String sql = "delete from logs";
        
        List<String> conds = new ArrayList<>();    
        if (opt.since().isPresent()) conds.add("createdAt > ?");
        if (opt.before().isPresent()) conds.add("createdAt < ?");
        if (opt.service().isPresent()) conds.add("service = ?");
        if (opt.level().isPresent()) conds.add("level = ?");

        if (conds.size() > 0) sql += " where ";
        for (int i = 0; i < conds.size(); i++) {
            sql += String.format(" %s ", conds.get(i));
            if (i != conds.size() - 1)
                sql += " and ";
        }

        PreparedStatement statement = handler.getConnection().prepareStatement(sql);
        int counter = 1;

        if (opt.since().isPresent()) {
            statement.setTimestamp(counter, Timestamp.valueOf(opt.since().get()));
            counter++;
        }

        if (opt.before().isPresent()) {
            statement.setTimestamp(counter, Timestamp.valueOf(opt.before().get()));
            counter++;
        }

        if (opt.service().isPresent()) {
            statement.setString(counter, opt.service().get());
            counter++;
        }

        if (opt.level().isPresent()) {
            statement.setString(counter, opt.level().get().toString());
            counter++;
        }

        statement.executeUpdate();
    }
    
    @Override
    public List<Log> get(Options opt, int limit) throws SQLException {
        String sql = "select * from logs";

        List<String> conds = new ArrayList<>();    
        if (opt.since().isPresent()) conds.add("createdAt > ?");
        if (opt.before().isPresent()) conds.add("createdAt < ?");
        if (opt.service().isPresent()) conds.add("service = ?");
        if (opt.level().isPresent()) conds.add("level = ?");

        if (conds.size() > 0) sql += " where ";
        for (int i = 0; i < conds.size(); i++) {
            sql += String.format(" %s ", conds.get(i));
            if (i != conds.size() - 1)
                sql += " and ";
        }

        sql += " order by createdAt limit ?";
        sql += ";";

        PreparedStatement statement = handler.getConnection().prepareStatement(sql);
        int counter = 1;
        
        if (opt.since().isPresent()) {
            statement.setTimestamp(counter, Timestamp.valueOf(opt.since().get()));
            counter++;
        }
        
        if (opt.before().isPresent()) {
            statement.setTimestamp(counter, Timestamp.valueOf(opt.before().get()));
            counter++;
        }
        
        if (opt.service().isPresent()) {
            statement.setString(counter, opt.service().get());
            counter++;
        }
        
        if (opt.level().isPresent()) {
            statement.setString(counter, opt.level().get().toString());
            counter++;
        }
        
        statement.setInt(counter, limit);

        ResultSet sqlResp = statement.executeQuery();
        List<Log> result = new ArrayList<>();

        while (sqlResp.next()) {
            String service = sqlResp.getString("service");
            Optional<ELevel> level = ELevel.fromString(sqlResp.getString("level"));
            if (level.isEmpty()) continue;
            String message = sqlResp.getString("message");
            String trace = sqlResp.getString("trace");
            Timestamp time = sqlResp.getTimestamp("createdAt");

            result.add(new Log(service, level.get(), message, trace, time));
        }

        return result;
    }
}
