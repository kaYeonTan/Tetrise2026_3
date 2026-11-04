package kr.ac.jbnu.se.tetris;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.Serial;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class LoginFrame extends JDialog {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final int FORM_X = 60;
    private static final int FORM_WIDTH = 400;

    private static final String SIGN_UP_TITLE = "회원가입";
    private static final String LOGIN_TITLE = "로그인";

    private final GamePanel gamePanel;
    private boolean signUpMode;
    private final JPanel rootPanel;
    private JTextField nicknameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField passwordConfirmField;
    private JLabel errorLabel;

    @SuppressWarnings("this-escape")
    public LoginFrame(GamePanel gamePanel, boolean signUpMode) {
        super(SwingUtilities.getWindowAncestor(gamePanel), modeTitle(signUpMode), ModalityType.APPLICATION_MODAL);
        this.gamePanel = gamePanel;
        this.signUpMode = signUpMode;
        setResizable(false);
        rootPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0;
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
                g.drawImage(Art.castle(), 0, 0, getWidth(), getHeight(), 300, 60, 300 + getWidth(), 60 + getHeight(), null);
                g.setColor(new Color(10, 7, 12, 110));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(UiTheme.INK);
                g.fillRect(30, 80, getWidth() - 60, getHeight() - 110);
                g.setColor(UiTheme.BRASS);
                g.fillRect(34, 84, getWidth() - 68, getHeight() - 118);
                g.setColor(UiTheme.TEXT_GOLD);
                g.fillRect(38, 88, getWidth() - 76, getHeight() - 126);
                g.setFont(LoginFrame.this.gamePanel.fontBold.deriveFont(33f));
                String title = modeTitle(LoginFrame.this.signUpMode);
                int titleWidth = g.getFontMetrics().stringWidth(title);
                g.setColor(UiTheme.TITLE_SHADOW);
                g.drawString(title, (getWidth() - titleWidth) / 2 + 3, 56 + 3);
                g.setColor(UiTheme.TEXT_LIGHT);
                g.drawString(title, (getWidth() - titleWidth) / 2, 56);
            }
        };
        rootPanel.setLayout(null);
        setContentPane(rootPanel);
        buildForm();
        setLocationRelativeTo(gamePanel);
    }

    private static String modeTitle(boolean signUpMode) {
        return signUpMode ? SIGN_UP_TITLE : LOGIN_TITLE;
    }

    private void buildForm() {
        rootPanel.removeAll();
        nicknameField = new JTextField();
        emailField = new JTextField();
        passwordField = new JPasswordField();
        passwordConfirmField = new JPasswordField();

        int y = 110;
        y = addFormRow(signUpMode ? "닉네임 (한글·영문·숫자 2~10자)" : "닉네임", nicknameField, y);
        if (signUpMode) {
            y = addFormRow("이메일", emailField, y);
        }
        y = addFormRow(signUpMode ? "비밀번호 (8자 이상)" : "비밀번호", passwordField, y);
        if (signUpMode) {
            y = addFormRow("비밀번호 확인", passwordConfirmField, y);
        }

        errorLabel = new JLabel(" ");
        errorLabel.setFont(gamePanel.font.deriveFont(16.5f));
        errorLabel.setForeground(UiTheme.DANGER);
        errorLabel.setBounds(FORM_X, y, FORM_WIDTH, 26);
        rootPanel.add(errorLabel);
        y += 34;

        JButton submitButton = createButton(signUpMode ? "가입하고 모험 시작" : LOGIN_TITLE, true);
        submitButton.setBounds(FORM_X, y, FORM_WIDTH, 50);
        submitButton.addActionListener(event -> submit());
        rootPanel.add(submitButton);
        y += 66;

        JLabel switchPromptLabel = new JLabel(signUpMode ? "이미 계정이 있나요?" : "아직 용사가 아니신가요?");
        switchPromptLabel.setFont(gamePanel.font.deriveFont(16.5f));
        switchPromptLabel.setForeground(UiTheme.INK);
        switchPromptLabel.setBounds(FORM_X, y, 230, 40);
        rootPanel.add(switchPromptLabel);
        JButton switchModeButton = createButton(modeTitle(!signUpMode), false);
        switchModeButton.setBounds(300, y, 160, 40);
        switchModeButton.addActionListener(event -> {
            signUpMode = !signUpMode;
            setTitle(modeTitle(signUpMode));
            buildForm();
        });
        rootPanel.add(switchModeButton);
        y += 90;

        rootPanel.setPreferredSize(new java.awt.Dimension(520, y));
        pack();
        setLocationRelativeTo(gamePanel);
        rootPanel.revalidate();
        rootPanel.repaint();

        bindEnterKeys();
    }

    /** Enter 키로 다음 입력칸으로 넘어가고, 마지막 칸에서는 제출한다. */
    private void bindEnterKeys() {
        nicknameField.addActionListener(event -> (signUpMode ? emailField : passwordField).requestFocusInWindow());
        emailField.addActionListener(event -> passwordField.requestFocusInWindow());
        passwordField.addActionListener(event -> {
            if (signUpMode) {
                passwordConfirmField.requestFocusInWindow();
            } else {
                submit();
            }
        });
        passwordConfirmField.addActionListener(event -> submit());
    }

    int addFormRow(String labelText, JTextField field, int y) {
        JLabel label = new JLabel(labelText);
        label.setFont(gamePanel.font.deriveFont(16.5f));
        label.setForeground(UiTheme.INK);
        label.setBounds(FORM_X, y, FORM_WIDTH, 24);
        rootPanel.add(label);
        field.setFont(gamePanel.font.deriveFont(22f));
        field.setBackground(UiTheme.PARCHMENT);
        field.setForeground(UiTheme.INK);
        field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UiTheme.INK, 3),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        field.setBounds(FORM_X, y + 28, FORM_WIDTH, 44);
        rootPanel.add(field);
        return y + 84;
    }

    JButton createButton(String text, boolean primary) {
        JButton button = new JButton(text);
        button.addActionListener(event -> gamePanel.playButtonClickSound());
        button.setFont(gamePanel.font.deriveFont(primary ? 22f : 16.5f));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setBackground(primary ? UiTheme.DANGER : UiTheme.PARCHMENT);
        button.setForeground(primary ? new Color(0xfff4e6) : UiTheme.INK);
        button.setBorder(BorderFactory.createLineBorder(UiTheme.INK, 4));
        return button;
    }

    void submit() {
        String nickname = nicknameField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (signUpMode) {
            submitSignUp(nickname, password);
        } else {
            submitLogin(nickname, password);
        }
    }

    private void submitSignUp(String nickname, String password) {
        String email = emailField.getText().trim();
        String passwordConfirm = new String(passwordConfirmField.getPassword());
        String error = SignUpValidator.validate(nickname, email, password, passwordConfirm);
        if (error == null) {
            error = FileManager.signUp(nickname, email, password);
        }
        if (error != null) {
            errorLabel.setText(error);
            return;
        }
        gamePanel.loginUser = nickname;
        dispose();
    }

    private void submitLogin(String nickname, String password) {
        String error = SignUpValidator.validateLoginInput(nickname, password);
        if (error != null) {
            errorLabel.setText(error);
            return;
        }
        if (!FileManager.login(nickname, password)) {
            errorLabel.setText("닉네임 또는 비밀번호가 올바르지 않아요.");
            passwordField.setText("");
            return;
        }
        gamePanel.loginUser = FileManager.findStoredNickname(nickname);
        dispose();
    }

}
