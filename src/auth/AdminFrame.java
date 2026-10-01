package auth;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;

/** Рабочий стол администратора: список пользователей, добавление, изменение, снятие блокировки. */
public class AdminFrame extends JFrame {
    private final UserDao userDao = new UserDao();
    private final User currentAdmin;
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Логин", "ФИО", "Роль", "Статус", "Неудачных попыток"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable usersTable = new JTable(tableModel);
    private List<User> loadedUsers = List.of();

    public AdminFrame(User currentAdmin) {
        super("Рабочий стол администратора — " + currentAdmin.getFullName());
        this.currentAdmin = currentAdmin;
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        usersTable.setRowHeight(24);

        JButton addButton = new JButton("Добавить пользователя");
        addButton.addActionListener(e -> onAdd());
        JButton editButton = new JButton("Изменить");
        editButton.addActionListener(e -> onEdit());
        JButton unblockButton = new JButton("Снять блокировку");
        unblockButton.addActionListener(e -> onUnblock());
        JButton logoutButton = new JButton("Выйти");
        logoutButton.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(addButton);
        buttons.add(editButton);
        buttons.add(unblockButton);
        buttons.add(logoutButton);

        add(buttons, BorderLayout.NORTH);
        add(new JScrollPane(usersTable), BorderLayout.CENTER);
        setSize(820, 420);
        setLocationRelativeTo(null);
        reloadTable();
    }

    private void reloadTable() {
        try {
            loadedUsers = userDao.findAll();
            tableModel.setRowCount(0);
            for (User user : loadedUsers) {
                tableModel.addRow(new Object[]{user.getId(), user.getLogin(), user.getFullName(), user.getRole(),
                        user.isBlocked() ? "Заблокирован" : "Активен", user.getFailedAttempts()});
            }
        } catch (SQLException e) {
            Messages.databaseError(this, e);
        }
    }

    /** Возвращает выбранного пользователя или null (с предупреждением). */
    private User getSelectedUser() {
        int row = usersTable.getSelectedRow();
        if (row < 0) {
            Messages.warning(this, "Пользователь не выбран.\nВыберите пользователя в таблице и повторите действие.");
            return null;
        }
        return loadedUsers.get(row);
    }

    private void onAdd() {
        UserDialog dialog = new UserDialog(this, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            reloadTable();
        }
    }

    private void onEdit() {
        User selected = getSelectedUser();
        if (selected == null) {
            return;
        }
        UserDialog dialog = new UserDialog(this, selected);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            reloadTable();
        }
    }

    private void onUnblock() {
        User selected = getSelectedUser();
        if (selected == null) {
            return;
        }
        if (!selected.isBlocked()) {
            Messages.info(this, "Пользователь «" + selected.getLogin() + "» не заблокирован.");
            return;
        }
        if (!Messages.confirm(this, "Снять блокировку с пользователя «" + selected.getLogin() + "»?")) {
            return;
        }
        try {
            userDao.setBlocked(selected.getId(), false);
            Messages.info(this, "Блокировка пользователя «" + selected.getLogin() + "» снята.");
            reloadTable();
        } catch (SQLException e) {
            Messages.databaseError(this, e);
        }
    }
}
