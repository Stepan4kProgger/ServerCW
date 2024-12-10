package JDBC;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class JDBC_class {
    private static Connection connection = null;

    private static final String database_name = "shopdatabase";

    private static final String root_password = "9196";

    public static Connection getConnection() {
        return connection;
    }

    public static void connect() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("JDBC драйвер не найден");
        }
        connection = DriverManager
                .getConnection("jdbc:mysql://localhost/" + database_name,
                        "root",
                        root_password);
        if (connection == null)
            throw new SQLException("Драйвер не подключен");
        System.out.println("БД успешно подключена к серверу.");
    }

    public static void close() {
        try {
            if (connection != null)
                connection.close();
        } catch (SQLException e) {
            System.out.println("Unable to close JDBC connection: " + e.getMessage());
        }
    }

    public static void createDB() throws SQLException {
        close();
        connection = DriverManager
                .getConnection("jdbc:mysql://localhost/mysql",
                        "root",
                        root_password);
        System.out.println("Подключение для создания бд установлено.");
        Statement statement = connection.createStatement();
        statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + database_name);
        System.out.println("БД создана.");

    }
}
