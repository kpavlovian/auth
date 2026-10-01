package auth;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

/** Рабочий стол пользователя с ролью «Пользователь». */
public class UserFrame extends JFrame {

    public UserFrame(User user) {
        super("Рабочий стол пользователя");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        content.add(new JLabel("Здравствуйте, " + user.getFullName() + "!", JLabel.CENTER), BorderLayout.CENTER);

        JButton logoutButton = new JButton("Выйти");
        logoutButton.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
        content.add(logoutButton, BorderLayout.SOUTH);

        setContentPane(content);
        setSize(380, 180);
        setLocationRelativeTo(null);
    }
}
