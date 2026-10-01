package auth;

/** Допустимые роли пользователей. */
public final class Roles {
    public static final String ADMINISTRATOR = "Администратор";
    public static final String USER = "Пользователь";
    public static final String[] ALL = {ADMINISTRATOR, USER};

    private Roles() { }
}
