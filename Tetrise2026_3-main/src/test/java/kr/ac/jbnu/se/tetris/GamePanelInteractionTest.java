package kr.ac.jbnu.se.tetris;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GamePanelInteractionTest {

    @TempDir
    File tempDir;

    private GamePanel panel;
    private String originalDataDir;
    private int[] originalSingleKeys;
    private int[] originalP1Keys;
    private int[] originalP2Keys;
    private int originalPauseKey;

    @BeforeEach
    void setUp() {
        originalDataDir = FileManager.DIR;
        originalSingleKeys = GamePanel.keySingle.clone();
        originalP1Keys = GamePanel.keyP1.clone();
        originalP2Keys = GamePanel.keyP2.clone();
        originalPauseKey = GamePanel.keyPause;

        FileManager.DIR = new File(tempDir, "data").getPath() + File.separator;
        GamePanel.keySingle = new int[]{KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN,
                KeyEvent.VK_SPACE, KeyEvent.VK_UP, KeyEvent.VK_Z, KeyEvent.VK_C};
        GamePanel.keyP1 = new int[]{KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_S,
                KeyEvent.VK_SPACE, KeyEvent.VK_W, KeyEvent.VK_Q, KeyEvent.VK_E};
        GamePanel.keyP2 = new int[]{KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN,
                KeyEvent.VK_ENTER, KeyEvent.VK_UP, KeyEvent.VK_SLASH, KeyEvent.VK_PERIOD};
        GamePanel.keyPause = KeyEvent.VK_P;

        panel = new GamePanel();
        panel.timer.stop();
    }

    @AfterEach
    void tearDown() {
        panel.timer.stop();
        FileManager.DIR = originalDataDir;
        GamePanel.keySingle = originalSingleKeys;
        GamePanel.keyP1 = originalP1Keys;
        GamePanel.keyP2 = originalP2Keys;
        GamePanel.keyPause = originalPauseKey;
    }

    @Test
    void guestCanStartSinglePlayerPauseResumeAndGetsSaveFeedback() {
        click(100, 280);
        assertEquals(1, panel.screen);
        assertTrue(panel.saveList.isEmpty());

        click(640, 635);
        assertEquals(2, panel.screen);
        click(450, 295);
        assertEquals(5, panel.screen);
        assertFalse(panel.isTwoPlayer);

        int startingX = panel.p1X;
        press(KeyEvent.VK_LEFT);
        assertEquals(startingX - 1, panel.p1X);
        press(KeyEvent.VK_ESCAPE);
        assertEquals(6, panel.screen);

        click(640, 310);
        assertTrue(panel.msg.contains("게스트는 저장할 수 없어요"));
        click(640, 235);
        assertEquals(5, panel.screen);
    }

    @Test
    void twoPlayerModeRespondsToBothPlayersControls() {
        click(100, 280);
        click(640, 635);
        click(480, 390);

        assertEquals(5, panel.screen);
        assertTrue(panel.isTwoPlayer);
        int p1StartingX = panel.p1X;
        int p2StartingX = panel.p2X;
        press(KeyEvent.VK_A);
        press(KeyEvent.VK_LEFT);
        assertEquals(p1StartingX - 1, panel.p1X);
        assertEquals(p2StartingX - 1, panel.p2X);
    }

    @Test
    void settingsChangesPersistAndRankingTabsSwitch() {
        click(100, 430);
        assertEquals(4, panel.screen);
        click(700, 205);
        press(KeyEvent.VK_J);
        click(750, 610);
        assertEquals(KeyEvent.VK_J, GamePanel.keySingle[0]);

        GamePanel.keySingle[0] = KeyEvent.VK_LEFT;
        FileManager.loadKeys();
        assertEquals(KeyEvent.VK_J, GamePanel.keySingle[0]);
        click(900, 610);
        assertEquals(0, panel.screen);

        FileManager.addRecord("tester", 500, 2, 1, false);
        click(100, 360);
        assertEquals(3, panel.screen);
        assertEquals(1, panel.rankList.size());
        click(510, 170);
        assertEquals(1, panel.rankTab);
        assertEquals("tester", panel.rankList.get(0)[0]);
        click(270, 170);
        assertEquals(0, panel.rankTab);
    }

    @Test
    void authenticatedSaveAppearsInListAndCanBeResumed() {
        panel.loginUser = "tester";
        panel.newGame(true);
        panel.p1Score = 120;
        assertTrue(FileManager.saveGame(panel));
        assertNotNull(panel.saveFile);

        panel.screen = 0;
        click(100, 280);
        assertEquals(1, panel.screen);
        assertEquals(1, panel.saveList.size());
        click(820, 185);

        assertEquals(5, panel.screen);
        assertTrue(panel.isTwoPlayer);
        assertEquals(120, panel.p1Score);
    }

    private void click(int x, int y) {
        panel.mousePressed(new MouseEvent(panel, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(),
                0, x, y, 1, false, MouseEvent.BUTTON1));
    }

    private void press(int keyCode) {
        panel.keyPressed(new KeyEvent(panel, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
                0, keyCode, KeyEvent.CHAR_UNDEFINED));
        panel.keyReleased(new KeyEvent(panel, KeyEvent.KEY_RELEASED, System.currentTimeMillis(),
                0, keyCode, KeyEvent.CHAR_UNDEFINED));
    }
}
