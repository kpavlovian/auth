package auth;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Frame;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/** Диалог добавления нового пользователя / изменения существующего. */
public class UserDialog extends JDialog {
    private final UserDao userDao = new UserDao();
    private final User editedUser; // null — режим добавления
    private boolean saved;

    private final JTextField loginField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JTextField fullNameField = new JTextField(20);
    private final JComboBox<String> roleComboBox = new JComboBox<>(Roles.ALL);

    public UserDialog(Frame owner, User editedUser) {
        super(owner, editedUser == null ? "Добавление пользователя" : "Изменение пользователя", true);
        this.editedUser = editedUser;

        if (editedUser != null) {
            loginField.setText(editedUser.getLogin());
            fullNameField.setText(editedUser.getFullName());
            roleComboBox.setSelectedItem(editedUser.getRole());
        }

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 8, 5, 8);
        c.anchor = GridBagConstraints.WEST;
        addRow(panel, c, 0, "Логин:", loginField);
        addRow(panel, c, 1, editedUser == null ? "Пароль:" : "Новый пароль:", passwordField);
        addRow(panel, c, 2, "ФИО:", fullNameField);
        addRow(panel, c, 3, "Роль:", roleComboBox);

        JButton saveButton = new JButton("Сохранить");
        saveButton.addActionListener(e -> onSave());
        JButton cancelButton = new JButton("Отмена");
        cancelButton.addActionListener(e -> dispose());
        c.gridx = 0; c.gridy = 4; c.gridwidth = 2; c.anchor = GridBagConstraints.CENTER;
        JPanel buttons = new JPanel();
        buttons.add(saveButton);
        buttons.add(cancelButton);
        panel.add(buttons, c);

        setContentPane(panel);
        getRootPane().setDefaultButton(saveButton);
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void addRow(JPanel panel, GridBagConstraints c, int row, String label, java.awt.Component field) {
        c.gridy = row; c.gridwidth = 1; c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        panel.add(new JLabel(label), c);
        c.gridx = 1;
        panel.add(field, c);
    }

    public boolean isSaved() {
        return saved;
    }

    private void onSave() {
        String login = loginField.getText().trim();
        String password = new String(passwordField.getPassword());
        String fullName = fullNameField.getText().trim();
        String role = (String) roleComboBox.getSelectedItem();

        if (login.isEmpty() || fullName.isEmpty()) {
            Messages.warning(this, "Поля «Логин» и «ФИО» обязательны для заполнения.");
            return;
        }
        if (editedUser == null && password.isEmpty()) {
            Messages.warning(this, "Для нового пользователя необходимо задать пароль.");
            return;
        }

        try {
            // Проверка на существование пользователя с таким логином
            User sameLogin = userDao.findByLogin(login);
            boolean loginTaken = sameLogin != null && (editedUser == null || sameLogin.getId() != editedUser.getId());
            if (loginTaken) {
                Messages.warning(this, "Пользователь с логином «" + login + "» уже существует.\n"
                        + "Укажите другой логин.");
                return;
            }

            if (editedUser == null) {
                userDao.create(login, password, fullName, role);
                Messages.info(this, "Пользователь «" + login + "» успешно добавлен.");
            } else {
                userDao.update(editedUser.getId(), login, password, fullName, role);
                Messages.info(this, "Данные пользователя «" + login + "» успешно обновлены.");
            }
            saved = true;
            dispose();
        } catch (SQLException e) {
            Messages.databaseError(this, e);
        }
    }
}
