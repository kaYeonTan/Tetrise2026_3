package kr.ac.jbnu.se.tetris;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class LoginFrame extends JDialog {

    private static final long serialVersionUID = 1L;

    private static final int FORM_X = 60;
    private static final int FORM_WIDTH = 400;

    private final GamePanel panel;
    private boolean isSignUp;
    private final JPanel root;
    private JTextField nickField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmField;
    private JLabel messageLabel;

    @SuppressWarnings("this-escape")
    public LoginFrame(GamePanel panel, boolean isSignUp) {
        super(SwingUtilities.getWindowAncestor(panel), isSignUp ? "회원가입" : "로그인", ModalityType.APPLICATION_MODAL);
        this.panel = panel;
        this.isSignUp = isSignUp;
        setResizable(false);
        root = new JPanel() {
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
                g.setFont(LoginFrame.this.panel.fontBold.deriveFont(33f));
                String title = LoginFrame.this.isSignUp ? "회원가입" : "로그인";
                int w = g.getFontMetrics().stringWidth(title);
                g.setColor(UiTheme.TITLE_SHADOW);
                g.drawString(title, (getWidth() - w) / 2 + 3, 56 + 3);
                g.setColor(UiTheme.TEXT_LIGHT);
                g.drawString(title, (getWidth() - w) / 2, 56);
            }
        };
        root.setLayout(null);
        setContentPane(root);
        build();
        setLocationRelativeTo(panel);
    }

    private void build() {
        root.removeAll();
        int y = 110;
        nickField = new JTextField();
        emailField = new JTextField();
        passwordField = new JPasswordField();
        confirmField = new JPasswordField();
        y = addRow(isSignUp ? "닉네임 (한글·영문·숫자 2~10자)" : "닉네임", nickField, y);
        if (isSignUp) {
            y = addRow("이메일", emailField, y);
        }
        y = addRow(isSignUp ? "비밀번호 (8자 이상)" : "비밀번호", passwordField, y);
        if (isSignUp) {
            y = addRow("비밀번호 확인", confirmField, y);
        }
        messageLabel = new JLabel(" ");
        messageLabel.setFont(panel.font.deriveFont(16.5f));
        messageLabel.setForeground(UiTheme.DANGER);
        messageLabel.setBounds(FORM_X, y, FORM_WIDTH, 26);
        root.add(messageLabel);
        y += 34;

        JButton ok = makeButton(isSignUp ? "가입하고 모험 시작" : "로그인", true);
        ok.setBounds(FORM_X, y, FORM_WIDTH, 50);
        ok.addActionListener(x -> submit());
        root.add(ok);
        y += 66;

        JLabel ask = new JLabel(isSignUp ? "이미 계정이 있나요?" : "아직 용사가 아니신가요?");
        ask.setFont(panel.font.deriveFont(16.5f));
        ask.setForeground(UiTheme.INK);
        ask.setBounds(FORM_X, y, 230, 40);
        root.add(ask);
        JButton change = makeButton(isSignUp ? "로그인" : "회원가입", false);
        change.setBounds(300, y, 160, 40);
        change.addActionListener(x -> {
            isSignUp = !isSignUp;
            setTitle(isSignUp ? "회원가입" : "로그인");
            build();
        });
        root.add(change);
        y += 90;

        root.setPreferredSize(new java.awt.Dimension(520, y));
        pack();
        setLocationRelativeTo(panel);
        root.revalidate();
        root.repaint();

        nickField.addActionListener(x -> (isSignUp ? emailField : passwordField).requestFocusInWindow());
        emailField.addActionListener(x -> passwordField.requestFocusInWindow());
        passwordField.addActionListener(x -> {
            if (isSignUp) {
                confirmField.requestFocusInWindow();
            } else {
                submit();
            }
        });
        confirmField.addActionListener(x -> submit());
    }

    int addRow(String label, JTextField field, int y) {
        JLabel l = new JLabel(label);
        l.setFont(panel.font.deriveFont(16.5f));
        l.setForeground(UiTheme.INK);
        l.setBounds(FORM_X, y, FORM_WIDTH, 24);
        root.add(l);
        field.setFont(panel.font.deriveFont(22f));
        field.setBackground(UiTheme.PARCHMENT);
        field.setForeground(UiTheme.INK);
        field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UiTheme.INK, 3),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        field.setBounds(FORM_X, y + 28, FORM_WIDTH, 44);
        root.add(field);
        return y + 84;
    }

    JButton makeButton(String text, boolean red) {
        JButton b = new JButton(text);
        b.addActionListener(e -> panel.playButtonClickSound());
        b.setFont(panel.font.deriveFont(red ? 22f : 16.5f));
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(true);
        b.setBackground(red ? UiTheme.DANGER : UiTheme.PARCHMENT);
        b.setForeground(red ? new Color(0xfff4e6) : UiTheme.INK);
        b.setBorder(BorderFactory.createLineBorder(UiTheme.INK, 4));
        return b;
    }

    void submit() {
        String nick = nickField.getText().trim();
        String pass = new String(passwordField.getPassword());
        if (isSignUp) {
            submitSignUp(nick, pass);
        } else {
            submitLogin(nick, pass);
        }
    }

    private void submitSignUp(String nick, String pass) {
        String mail = emailField.getText().trim();
        String pass2 = new String(confirmField.getPassword());
        String error = SignUpValidator.validate(nick, mail, pass, pass2);
        if (error != null) {
            messageLabel.setText(error);
            return;
        }
        String result = FileManager.addUser(nick, mail, pass);
        if (result != null) {
            messageLabel.setText(result);
            return;
        }
        panel.loginUser = nick;
        dispose();
    }

    private void submitLogin(String nick, String pass) {
        String error = SignUpValidator.validateLoginInput(nick, pass);
        if (error != null) {
            messageLabel.setText(error);
            return;
        }
        if (FileManager.checkUser(nick, pass)) {
            panel.loginUser = FileManager.getNick(nick);
            dispose();
        } else {
            messageLabel.setText("닉네임 또는 비밀번호가 올바르지 않아요.");
            passwordField.setText("");
        }
    }

}
