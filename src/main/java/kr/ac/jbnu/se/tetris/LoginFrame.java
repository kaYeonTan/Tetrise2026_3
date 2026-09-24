package kr.ac.jbnu.se.tetris;

import java.awt.Color;
import java.awt.Font;
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

    GamePanel panel;
    boolean isSignUp;
    JPanel content;
    JTextField nickField;
    JTextField emailField;
    JPasswordField pwField;
    JPasswordField pw2Field;
    JLabel msgLabel;

    public LoginFrame(GamePanel panel, boolean isSignUp) {
        super(SwingUtilities.getWindowAncestor(panel), isSignUp ? "회원가입" : "로그인", ModalityType.APPLICATION_MODAL);
        this.panel = panel;
        this.isSignUp = isSignUp;
        setResizable(false);
        content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0;
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
                g.drawImage(Art.castle(), 0, 0, getWidth(), getHeight(), 300, 60, 300 + getWidth(), 60 + getHeight(), null);
                g.setColor(new Color(10, 7, 12, 110));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(new Color(0x1a1216));
                g.fillRect(30, 80, getWidth() - 60, getHeight() - 110);
                g.setColor(new Color(0xb89c6c));
                g.fillRect(34, 84, getWidth() - 68, getHeight() - 118);
                g.setColor(new Color(0xd9c29a));
                g.fillRect(38, 88, getWidth() - 76, getHeight() - 126);
                g.setFont(LoginFrame.this.panel.fontBold.deriveFont(33f));
                String title = LoginFrame.this.isSignUp ? "회원가입" : "로그인";
                int w = g.getFontMetrics().stringWidth(title);
                g.setColor(new Color(0x6b2a1a));
                g.drawString(title, (getWidth() - w) / 2 + 3, 56 + 3);
                g.setColor(new Color(0xf0e2c8));
                g.drawString(title, (getWidth() - w) / 2, 56);
            }
        };
        content.setLayout(null);
        setContentPane(content);
        build();
        setLocationRelativeTo(panel);
    }

    void build() {
        content.removeAll();
        int y = 110;
        nickField = new JTextField();
        emailField = new JTextField();
        pwField = new JPasswordField();
        pw2Field = new JPasswordField();
        y = addRow(isSignUp ? "닉네임 (한글·영문·숫자 2~10자)" : "닉네임", nickField, y);
        if (isSignUp) {
            y = addRow("이메일", emailField, y);
        }
        y = addRow(isSignUp ? "비밀번호 (8자 이상)" : "비밀번호", pwField, y);
        if (isSignUp) {
            y = addRow("비밀번호 확인", pw2Field, y);
        }
        msgLabel = new JLabel(" ");
        msgLabel.setFont(panel.font.deriveFont(16.5f));
        msgLabel.setForeground(new Color(0xa8352a));
        msgLabel.setBounds(60, y, 400, 26);
        content.add(msgLabel);
        y += 34;

        JButton ok = makeButton(isSignUp ? "가입하고 모험 시작" : "로그인", true);
        ok.setBounds(60, y, 400, 50);
        ok.addActionListener(e -> submit());
        content.add(ok);
        y += 66;

        JLabel ask = new JLabel(isSignUp ? "이미 계정이 있나요?" : "아직 용사가 아니신가요?");
        ask.setFont(panel.font.deriveFont(16.5f));
        ask.setForeground(new Color(0x1a1216));
        ask.setBounds(60, y, 230, 40);
        content.add(ask);
        JButton change = makeButton(isSignUp ? "로그인" : "회원가입", false);
        change.setBounds(300, y, 160, 40);
        change.addActionListener(e -> {
            isSignUp = !isSignUp;
            setTitle(isSignUp ? "회원가입" : "로그인");
            build();
        });
        content.add(change);
        y += 90;

        content.setPreferredSize(new java.awt.Dimension(520, y));
        pack();
        setLocationRelativeTo(panel);
        content.revalidate();
        content.repaint();

        nickField.addActionListener(e -> (isSignUp ? emailField : pwField).requestFocusInWindow());
        emailField.addActionListener(e -> pwField.requestFocusInWindow());
        pwField.addActionListener(e -> {
            if (isSignUp) {
                pw2Field.requestFocusInWindow();
            } else {
                submit();
            }
        });
        pw2Field.addActionListener(e -> submit());
    }

    int addRow(String label, JTextField field, int y) {
        JLabel l = new JLabel(label);
        l.setFont(panel.font.deriveFont(16.5f));
        l.setForeground(new Color(0x1a1216));
        l.setBounds(60, y, 400, 24);
        content.add(l);
        field.setFont(panel.font.deriveFont(22f));
        field.setBackground(new Color(0xefe0bf));
        field.setForeground(new Color(0x1a1216));
        field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0x1a1216), 3),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        field.setBounds(60, y + 28, 400, 44);
        content.add(field);
        return y + 84;
    }

    JButton makeButton(String text, boolean red) {
        JButton b = new JButton(text);
        b.setFont(panel.font.deriveFont(red ? 22f : 16.5f));
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(true);
        b.setBackground(red ? new Color(0xa8352a) : new Color(0xefe0bf));
        b.setForeground(red ? new Color(0xfff4e6) : new Color(0x1a1216));
        b.setBorder(BorderFactory.createLineBorder(new Color(0x1a1216), 4));
        return b;
    }

    void submit() {
        String nick = nickField.getText().trim();
        String pw = new String(pwField.getPassword());
        if (isSignUp) {
            String email = emailField.getText().trim();
            String pw2 = new String(pw2Field.getPassword());
            if (!nick.matches("^[가-힣A-Za-z0-9]{2,10}$")) {
                msgLabel.setText("닉네임은 한글·영문·숫자 2~10자로 입력해 주세요.");
                return;
            }
            if (!email.matches("^[^@\\s,]+@[^@\\s,]+\\.[^@\\s,]+$")) {
                msgLabel.setText("이메일 형식이 올바르지 않아요.");
                return;
            }
            if (pw.length() < 8 || pw.length() > 64) {
                msgLabel.setText("비밀번호는 8~64자로 입력해 주세요.");
                return;
            }
            // 쉼표 들어가면 파일 읽을때 깨짐
            if (pw.contains(",")) {
                msgLabel.setText("비밀번호에 쉼표(,)는 쓸 수 없어요.");
                return;
            }
            if (!pw.equals(pw2)) {
                msgLabel.setText("비밀번호 확인이 일치하지 않아요.");
                return;
            }
            String result = FileManager.addUser(nick, email, pw);
            if (result != null) {
                msgLabel.setText(result);
                return;
            }
            panel.loginUser = nick;
            dispose();
        } else {
            if (nick.length() == 0 || pw.length() == 0) {
                msgLabel.setText("닉네임과 비밀번호를 모두 입력해 주세요.");
                return;
            }
            if (FileManager.checkUser(nick, pw)) {
                panel.loginUser = FileManager.getNick(nick);
                dispose();
            } else {
                msgLabel.setText("닉네임 또는 비밀번호가 올바르지 않아요.");
                pwField.setText("");
            }
        }
    }

}
