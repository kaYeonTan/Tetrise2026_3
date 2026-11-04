package kr.ac.jbnu.se.tetris;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PlayerTest {

    private Player newPlayer(int type) {
        Player p = new Player();
        p.type = type;
        p.startPiece();
        return p;
    }

    @Test
    void moveStopsAtWall() {
        Player p = newPlayer(2);
        int guard = 0;
        while (p.move(-1) && guard++ < 20) {
        }
        assertFalse(p.move(-1));
        assertTrue(p.move(1));
    }

    @Test
    void softDropAddsOnePointPerRow() {
        Player p = newPlayer(2);
        int y = p.y;
        assertTrue(p.softDrop());
        assertEquals(y + 1, p.y);
        assertEquals(1, p.score);
    }

    @Test
    void dropToFloorAddsTwoPointsPerRowAndRests() {
        Player p = newPlayer(2);
        int y = p.y;
        p.dropToFloor();
        assertFalse(p.fall());
        assertEquals((p.y - y) * 2, p.score);
    }

    @Test
    void oPieceDoesNotRotate() {
        Player p = newPlayer(1);
        assertFalse(p.rotate(1));
        assertEquals(0, p.rot);
    }

    @Test
    void holdStoresThenSwaps() {
        Player p = newPlayer(2);
        assertTrue(p.stashCurrent());
        assertEquals(2, p.hold);
        p.type = 3;
        assertFalse(p.stashCurrent());
        assertEquals(2, p.type);
        assertEquals(3, p.hold);
    }

    @Test
    void lineScoreScalesWithStage() {
        Player p = new Player();
        p.addLineScore(2, 3);
        assertEquals(2, p.lines);
        assertEquals(900, p.score);
    }

    @Test
    void placeOnBoardWritesCells() {
        Player p = newPlayer(2);
        p.dropToFloor();
        p.placeOnBoard();
        int filled = 0;
        for (int[] row : p.board) {
            for (int c : row) {
                if (c != 0) {
                    filled++;
                }
            }
        }
        assertEquals(GamePanel.blockCount(2), filled);
    }

    @Test
    void copyIsDeepAndRestorable() {
        Player p = newPlayer(2);
        p.next.add(4);
        p.board[23][0] = 1;
        p.score = 50;
        Player c = p.copy();
        p.board[23][0] = 0;
        p.next.clear();
        p.score = 0;
        assertEquals(1, c.board[23][0]);
        assertEquals(1, c.next.size());
        assertEquals(50, c.score);
        p.copyFrom(c);
        assertEquals(1, p.board[23][0]);
        assertEquals(50, p.score);
    }

    @Test
    void repeatLeftFiresAfterDelay() {
        Player p = new Player();
        assertFalse(p.repeatLeft(1));
        assertTrue(p.repeatLeft(GamePanel.HORIZONTAL_DAS_DELAY_MS));
    }
}
