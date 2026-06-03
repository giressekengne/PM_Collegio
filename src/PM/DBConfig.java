package PM;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConfig {

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        Properties props = new Properties();
        try (InputStream in = DBConfig.class.getClassLoader().getResourceAsStream("config/db.properties")) {
            if (in == null) throw new RuntimeException("db.properties non trovato nel classpath");
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Errore caricamento db.properties", e);
        }
        URL      = props.getProperty("db.url");
        USER     = props.getProperty("db.user");
        PASSWORD = props.getProperty("db.password");
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
