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

    GamePanel panel;
    boolean isSignUp;
    JPanel p;
    JTextField n;
    JTextField e;
    JPasswordField pw;
    JPasswordField pw2;
    JLabel msg;

    @SuppressWarnings("this-escape")
    public LoginFrame(GamePanel panel, boolean isSignUp) {
        super(SwingUtilities.getWindowAncestor(panel), isSignUp ? "회원가입" : "로그인", ModalityType.APPLICATION_MODAL);
        this.panel = panel;
        this.isSignUp = isSignUp;
        setResizable(false);
        p = new JPanel() {
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
        p.setLayout(null);
        setContentPane(p);
        build();
        setLocationRelativeTo(panel);
    }

    private void build() {
        p.removeAll();
        int y = 110;
        n = new JTextField();
        e = new JTextField();
        pw = new JPasswordField();
        pw2 = new JPasswordField();
        y = addRow(isSignUp ? "닉네임 (한글·영문·숫자 2~10자)" : "닉네임", n, y);
        if (isSignUp) {
            y = addRow("이메일", e, y);
        }
        y = addRow(isSignUp ? "비밀번호 (8자 이상)" : "비밀번호", pw, y);
        if (isSignUp) {
            y = addRow("비밀번호 확인", pw2, y);
        }
        msg = new JLabel(" ");
        msg.setFont(panel.font.deriveFont(16.5f));
        msg.setForeground(new Color(0xa8352a));
        msg.setBounds(60, y, 400, 26);
        p.add(msg);
        y += 34;

        JButton ok = makeButton(isSignUp ? "가입하고 모험 시작" : "로그인", true);
        ok.setBounds(60, y, 400, 50);
        ok.addActionListener(x -> submit());
        p.add(ok);
        y += 66;

        JLabel ask = new JLabel(isSignUp ? "이미 계정이 있나요?" : "아직 용사가 아니신가요?");
        ask.setFont(panel.font.deriveFont(16.5f));
        ask.setForeground(new Color(0x1a1216));
        ask.setBounds(60, y, 230, 40);
        p.add(ask);
        JButton change = makeButton(isSignUp ? "로그인" : "회원가입", false);
        change.setBounds(300, y, 160, 40);
        change.addActionListener(x -> {
            isSignUp = !isSignUp;
            setTitle(isSignUp ? "회원가입" : "로그인");
            build();
        });
        p.add(change);
        y += 90;

        p.setPreferredSize(new java.awt.Dimension(520, y));
        pack();
        setLocationRelativeTo(panel);
        p.revalidate();
        p.repaint();

        n.addActionListener(x -> (isSignUp ? e : pw).requestFocusInWindow());
        e.addActionListener(x -> pw.requestFocusInWindow());
        pw.addActionListener(x -> {
            if (isSignUp) {
                pw2.requestFocusInWindow();
            } else {
                submit();
            }
        });
        pw2.addActionListener(x -> submit());
    }

    int addRow(String label, JTextField field, int y) {
        JLabel l = new JLabel(label);
        l.setFont(panel.font.deriveFont(16.5f));
        l.setForeground(new Color(0x1a1216));
        l.setBounds(60, y, 400, 24);
        p.add(l);
        field.setFont(panel.font.deriveFont(22f));
        field.setBackground(new Color(0xefe0bf));
        field.setForeground(new Color(0x1a1216));
        field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0x1a1216), 3),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        field.setBounds(60, y + 28, 400, 44);
        p.add(field);
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
        String nick = n.getText().trim();
        String pass = new String(pw.getPassword());
        if (isSignUp) {
            String mail = e.getText().trim();
            String pass2 = new String(pw2.getPassword());
            if (nick.matches("^[가-힣A-Za-z0-9]{2,10}$")) {
                if (mail.matches("^[^@\\s,]+@[^@\\s,]+\\.[^@\\s,]+$")) {
                    if (pass.length() >= 8 && pass.length() <= 64) {
                        if (!pass.contains(",")) {
                            if (pass.equals(pass2)) {
                                String result = FileManager.addUser(nick, mail, pass);
                                if (result != null) {
                                    msg.setText(result);
                                    return;
                                }
                                panel.loginUser = nick;
                                dispose();
                            } else {
                                msg.setText("비밀번호 확인이 일치하지 않아요.");
                            }
                        } else {
                            msg.setText("비밀번호에 쉼표(,)는 쓸 수 없어요.");
                        }
                    } else {
                        msg.setText("비밀번호는 8~64자로 입력해 주세요.");
                    }
                } else {
                    msg.setText("이메일 형식이 올바르지 않아요.");
                }
            } else {
                msg.setText("닉네임은 한글·영문·숫자 2~10자로 입력해 주세요.");
            }
        } else {
            if (nick.length() == 0) {
                msg.setText("닉네임과 비밀번호를 모두 입력해 주세요.");
            } else {
                if (pass.length() == 0) {
                    msg.setText("닉네임과 비밀번호를 모두 입력해 주세요.");
                } else {
                    if (FileManager.checkUser(nick, pass)) {
                        panel.loginUser = FileManager.getNick(nick);
                        dispose();
                    } else {
                        msg.setText("닉네임 또는 비밀번호가 올바르지 않아요.");
                        pw.setText("");
                    }
                }
            }
        }
    }

}
