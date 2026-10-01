package auth;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Подключение к БД. По умолчанию — файл SQLite (не требует установки сервера).
 * Для PostgreSQL/MySQL достаточно заменить URL (и добавить драйвер в classpath):
 *   jdbc:postgresql://localhost:5432/mydb  |  jdbc:mysql://localhost:3306/mydb
 * URL, логин и пароль можно передать через -Ddb.url, -Ddb.user, -Ddb.password.
 */
public final class Database {
    private static final String URL = System.getProperty("db.url", "jdbc:sqlite:app.db");
    private static final String USER = System.getProperty("db.user", "");
    private static final String PASSWORD = System.getProperty("db.password", "");

    private Database() { }

    public static Connection open() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /** Создаёт таблицу пользователей и учётную запись администратора по умолчанию. */
    public static void initialize() throws SQLException {
        try (Connection connection = open(); Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "login VARCHAR(50) NOT NULL UNIQUE, "
                + "password_hash VARCHAR(100) NOT NULL, "
                + "salt VARCHAR(50) NOT NULL, "
                + "full_name VARCHAR(150) NOT NULL, "
                + "role VARCHAR(20) NOT NULL, "
                + "is_blocked INTEGER NOT NULL DEFAULT 0, "
                + "failed_attempts INTEGER NOT NULL DEFAULT 0)");
        }
        UserDao dao = new UserDao();
        if (!dao.existsByLogin("admin")) {
            dao.create("admin", "admin123", "Администратор системы", Roles.ADMINISTRATOR);
        }
        if (!dao.existsByLogin("user")) {
            dao.create("user", "user123", "Тестовый пользователь", Roles.USER);
        }
    }
}
