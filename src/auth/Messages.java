package auth;

import java.awt.Component;
import javax.swing.JOptionPane;

/** Окна сообщений с заголовком и пиктограммой соответствующего типа. */
public final class Messages {
    private Messages() { }

    public static void error(Component parent, String text) {
        JOptionPane.showMessageDialog(parent, text, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    public static void warning(Component parent, String text) {
        JOptionPane.showMessageDialog(parent, text, "Предупреждение", JOptionPane.WARNING_MESSAGE);
    }

    public static void info(Component parent, String text) {
        JOptionPane.showMessageDialog(parent, text, "Информация", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String text) {
        return JOptionPane.showConfirmDialog(parent, text, "Подтверждение",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public static void databaseError(Component parent, Exception e) {
        error(parent, "Не удалось выполнить операцию с базой данных.\n"
                + "Проверьте подключение к базе данных и повторите попытку.\n\nПодробности: " + e.getMessage());
    }
}
