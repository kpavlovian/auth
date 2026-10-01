package auth;

/** Пользователь системы (строка таблицы users). */
public class User {
    private final int id;
    private final String login;
    private final String fullName;
    private final String role;
    private final boolean blocked;
    private final int failedAttempts;

    public User(int id, String login, String fullName, String role, boolean blocked, int failedAttempts) {
        this.id = id;
        this.login = login;
        this.fullName = fullName;
        this.role = role;
        this.blocked = blocked;
        this.failedAttempts = failedAttempts;
    }

    public int getId() { return id; }
    public String getLogin() { return login; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
    public boolean isBlocked() { return blocked; }
    public int getFailedAttempts() { return failedAttempts; }
    public boolean isAdministrator() { return Roles.ADMINISTRATOR.equals(role); }
}
