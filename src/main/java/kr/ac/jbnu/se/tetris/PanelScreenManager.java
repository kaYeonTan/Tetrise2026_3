package kr.ac.jbnu.se.tetris;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

/** 화면 번호 → PanelScreen 매핑. 새 화면은 register 한 줄로 추가한다. */
public final class PanelScreenManager {
    private final Map<Integer, PanelScreen> screens = new HashMap<>();

    public void register(int id, PanelScreen screen) {
        screens.put(id, screen);
    }

    public boolean has(int id) {
        return screens.containsKey(id);
    }

    public boolean render(int id, Graphics2D g, GamePanel panel) {
        PanelScreen s = screens.get(id);
        if (s == null) {
            return false;
        }
        s.render(g, panel);
        return true;
    }

    public boolean click(int id, GamePanel panel) {
        PanelScreen s = screens.get(id);
        return s != null && s.handleClick(panel);
    }

    public boolean key(int id, GamePanel panel, KeyEvent e) {
        PanelScreen s = screens.get(id);
        return s != null && s.handleKey(panel, e);
    }

    public void update(int id, GamePanel panel) {
        PanelScreen s = screens.get(id);
        if (s != null) {
            s.update(panel);
        }
    }
}
