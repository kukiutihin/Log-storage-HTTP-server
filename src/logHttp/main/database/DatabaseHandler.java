package database;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseHandler {
    private Connection conn;

    public DatabaseHandler(String url, String user, String password) throws SQLException {
        conn = DriverManager.getConnection(url, user, password);
        init();
    }

    private void init() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("""
            create table if not exists logs(
                id bigint generated always as identity primary key,
                service varchar(50) not null,
                level varchar(50) not null,
                message varchar(255) not null,
                trace varchar(255) not null,
                createdAt timestamp not null default current_timestamp
            );
        """);
    }

    public Connection getConnection() {
        return conn;
    }
}
