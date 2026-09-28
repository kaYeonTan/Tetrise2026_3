package kr.ac.jbnu.se.tetris;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GamePanelTest {

    GamePanel p;

    @BeforeEach
    void setUp() {
        FileManager.DIR = "target/testdata/";
        p = new GamePanel();
        p.timer.stop();
        p.newGame(false);
    }

    @Test
    void bagTest() {
        HashSet<Integer> set = new HashSet<>();
        set.add(p.p1Type);
        for (int i = 0; i < 6; i++) {
            set.add(p.p1Next.get(i));
        }
        assertEquals(7, set.size());
    }

    @Test
    void tetrisDamage() {
        for (int c = 0; c < 9; c++) {
            for (int r = 20; r < 24; r++) {
                p.board1[r][c] = 8;
            }
        }
        p.p1Type = 0;
        p.p1Rot = 1;
        p.p1X = 7;
        p.p1Y = 3;
        p.hardDropP1();
        assertEquals(4, p.p1Lines);
        assertEquals(40, p.bossHp);
    }

    @Test
    void wallKick() {
        p.p1Type = 2;
        p.p1Rot = 1;
        p.p1X = -1;
        p.p1Y = 10;
        p.rotateP1(1);
        assertEquals(2, p.p1Rot);
        assertTrue(p.p1X >= 0);
    }

    @Test
    void holdOnce() {
        int first = p.p1Type;
        p.holdP1();
        p.holdP1();
        assertEquals(first, p.p1Hold);
    }

    @Test
    void saveLoad() {
        p.newGame(true);
        p.loginUser = "tester";
        p.p1Score = 500;
        p.board2[23][3] = 5;
        assertTrue(FileManager.saveGame(p));
        GamePanel q = new GamePanel();
        q.timer.stop();
        assertTrue(FileManager.loadGame(p.saveFile, q));
        assertEquals(500, q.p1Score);
        assertEquals(5, q.board2[23][3]);
        assertTrue(q.isTwoPlayer);
        FileManager.deleteSave(p.saveFile);
    }
}
