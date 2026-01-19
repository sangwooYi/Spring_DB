package hello.jdbc.connection;

import lombok.extern.slf4j.Slf4j;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// static 상수들 미리 import
import static hello.jdbc.connection.ConnectionConst.*;

@Slf4j
public class DBConnectionUtil {

    public static Connection getConnection() {

        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            log.info("get connection = {}. class = {}", connection, connection.getClass());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return connection;

    }

}
