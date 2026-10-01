package auth;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

/** Форма авторизации с капчей-пазлом и блокировкой после 3 неудачных попыток подряд. */
public class LoginFrame extends JFrame {
    private static final int MAX_FAILED_ATTEMPTS = 3;

    private static final String MSG_WRONG_CREDENTIALS =
            "Вы ввели неверный логин или пароль. Пожалуйста проверьте ещё раз введенные данные";
    private static final String MSG_BLOCKED = "Вы заблокированы. Обратитесь к администратору";
    private static final String MSG_SUCCESS = "Вы успешно авторизовались";

    private final UserDao userDao = new UserDao();
    private final JTextField loginField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JButton loginButton = new JButton("Войти");
    private PuzzleCaptchaPanel captchaPanel;

    public LoginFrame() {
        super("Авторизация");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        content.add(buildFormPanel(), BorderLayout.NORTH);

        try {
            captchaPanel = new PuzzleCaptchaPanel();
            content.add(captchaPanel, BorderLayout.CENTER);
        } catch (IOException e) {
            Messages.error(this, "Не удалось загрузить изображение для проверки (капчи).\n"
                    + "Убедитесь, что папка resources/captcha доступна приложению.\n\nПодробности: " + e.getMessage());
            System.exit(1);
        }

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        loginButton.setPreferredSize(new Dimension(140, 32));
        loginButton.addActionListener(e -> onLoginClicked());
        buttonPanel.add(loginButton);
        content.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(content);
        getRootPane().setDefaultButton(loginButton);
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel buildFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 0;
        form.add(new JLabel("Логин:"), c);
        c.gridx = 1;
        form.add(loginField, c);
        c.gridx = 0; c.gridy = 1;
        form.add(new JLabel("Пароль:"), c);
        c.gridx = 1;
        form.add(passwordField, c);
        return form;
    }

    private void onLoginClicked() {
        String login = loginField.getText().trim();
        String password = new String(passwordField.getPassword());

        // Поля обязательны для заполнения
        if (login.isEmpty() || password.isEmpty()) {
            Messages.warning(this, "Поля «Логин» и «Пароль» обязательны для заполнения.\n"
                    + "Введите логин и пароль и повторите попытку.");
            (login.isEmpty() ? loginField : passwordField).requestFocus();
            return;
        }

        try {
            User user = userDao.findByLogin(login);

            if (user != null && user.isBlocked()) {
                Messages.error(this, MSG_BLOCKED);
                captchaPanel.shuffle();
                return;
            }

            boolean puzzleSolved = captchaPanel.isSolved();
            boolean passwordCorrect = user != null && userDao.checkPassword(login, password);

            if (puzzleSolved && passwordCorrect) {
                userDao.resetFailedAttempts(user.getId());
                Messages.info(this, MSG_SUCCESS);
                openWorkDesk(user);
                return;
            }

            // Ошибка: неверный пазл или неверный логин/пароль
            boolean justBlocked = false;
            if (user != null) {
                justBlocked = userDao.registerFailedAttempt(user.getId(), MAX_FAILED_ATTEMPTS);
            }
            captchaPanel.shuffle();
            passwordField.setText("");

            if (justBlocked) {
                Messages.error(this, MSG_BLOCKED);
            } else if (!puzzleSolved) {
                Messages.error(this, "Картинка собрана неверно.\nРасположите фрагменты в правильном порядке "
                        + "(кликайте на два фрагмента, чтобы поменять их местами) и повторите попытку.\n"
                        + "После " + MAX_FAILED_ATTEMPTS + " ошибок подряд учётная запись будет заблокирована.");
            } else {
                Messages.error(this, MSG_WRONG_CREDENTIALS);
            }
        } catch (SQLException e) {
            Messages.databaseError(this, e);
        }
    }

    /** Открывает рабочий стол в зависимости от роли. */
    private void openWorkDesk(User user) {
        dispose();
        if (user.isAdministrator()) {
            new AdminFrame(user).setVisible(true);
        } else {
            new UserFrame(user).setVisible(true);
        }
    }
}
