package auth;

import java.sql.SQLException;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Точка входа приложения. */
public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Не критично: используем стандартный внешний вид
        }
        SwingUtilities.invokeLater(() -> {
            try {
                Database.initialize();
                new LoginFrame().setVisible(true);
            } catch (SQLException e) {
                Messages.databaseError(null, e);
                System.exit(1);
            }
        });
    }
}
