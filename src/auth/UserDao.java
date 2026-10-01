package auth;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Доступ к таблице users. Все запросы параметризованные (защита от SQL-инъекций). */
public class UserDao {

    private static final String COLUMNS = "id, login, full_name, role, is_blocked, failed_attempts";

    private User map(ResultSet rs) throws SQLException {
        return new User(rs.getInt("id"), rs.getString("login"), rs.getString("full_name"),
                rs.getString("role"), rs.getInt("is_blocked") == 1, rs.getInt("failed_attempts"));
    }

    public boolean existsByLogin(String login) throws SQLException {
        try (Connection c = Database.open();
             PreparedStatement ps = c.prepareStatement("SELECT 1 FROM users WHERE login = ?")) {
            ps.setString(1, login);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public User findByLogin(String login) throws SQLException {
        try (Connection c = Database.open();
             PreparedStatement ps = c.prepareStatement("SELECT " + COLUMNS + " FROM users WHERE login = ?")) {
            ps.setString(1, login);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /** Проверяет пару логин/пароль для уже найденного пользователя. */
    public boolean checkPassword(String login, String password) throws SQLException {
        try (Connection c = Database.open();
             PreparedStatement ps = c.prepareStatement("SELECT password_hash, salt FROM users WHERE login = ?")) {
            ps.setString(1, login);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                return rs.getString("password_hash").equals(PasswordHasher.hash(password, rs.getString("salt")));
            }
        }
    }

    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        try (Connection c = Database.open();
             PreparedStatement ps = c.prepareStatement("SELECT " + COLUMNS + " FROM users ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(map(rs));
            }
        }
        return users;
    }

    public void create(String login, String password, String fullName, String role) throws SQLException {
        String salt = PasswordHasher.generateSalt();
        try (Connection c = Database.open();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO users (login, password_hash, salt, full_name, role) VALUES (?, ?, ?, ?, ?)")) {
            ps.setString(1, login);
            ps.setString(2, PasswordHasher.hash(password, salt));
            ps.setString(3, salt);
            ps.setString(4, fullName);
            ps.setString(5, role);
            ps.executeUpdate();
        }
    }

    /** Обновляет данные; если newPassword пустой — пароль не меняется. */
    public void update(int id, String login, String newPassword, String fullName, String role) throws SQLException {
        try (Connection c = Database.open()) {
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE users SET login = ?, full_name = ?, role = ? WHERE id = ?")) {
                ps.setString(1, login);
                ps.setString(2, fullName);
                ps.setString(3, role);
                ps.setInt(4, id);
                ps.executeUpdate();
            }
            if (newPassword != null && !newPassword.isEmpty()) {
                String salt = PasswordHasher.generateSalt();
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE users SET password_hash = ?, salt = ? WHERE id = ?")) {
                    ps.setString(1, PasswordHasher.hash(newPassword, salt));
                    ps.setString(2, salt);
                    ps.setInt(3, id);
                    ps.executeUpdate();
                }
            }
        }
    }

    public void setBlocked(int id, boolean blocked) throws SQLException {
        try (Connection c = Database.open();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE users SET is_blocked = ?, failed_attempts = 0 WHERE id = ?")) {
            ps.setInt(1, blocked ? 1 : 0);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void resetFailedAttempts(int id) throws SQLException {
        try (Connection c = Database.open();
             PreparedStatement ps = c.prepareStatement("UPDATE users SET failed_attempts = 0 WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Фиксирует неудачную попытку входа. На 3-й подряд ошибке блокирует учётную запись.
     * @return true, если учётная запись только что заблокирована
     */
    public boolean registerFailedAttempt(int id, int maxAttempts) throws SQLException {
        try (Connection c = Database.open()) {
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE users SET failed_attempts = failed_attempts + 1 WHERE id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE users SET is_blocked = 1 WHERE id = ? AND failed_attempts >= ?")) {
                ps.setInt(1, id);
                ps.setInt(2, maxAttempts);
                if (ps.executeUpdate() > 0) {
                    return true;
                }
            }
            return false;
        }
    }
}
