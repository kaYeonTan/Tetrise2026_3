package kr.ac.jbnu.se.tetris.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import kr.ac.jbnu.se.tetris.Tetrominoes;
import org.junit.jupiter.api.Test;

class GameSessionBossAttackTest {

    @Test
    void risingLineMovesBoardAndActivePieceTogether() {
        GameSession session = new GameSession(0);
        assertTrue(session.spawnShape(Tetrominoes.TShape));
        assertTrue(session.tryMove(session.curPiece, session.curX, session.curY - 5));
        int oldY = session.curY;
        session.cells[0] = Tetrominoes.ZShape;

        assertTrue(session.raiseGarbageLine(3));

        assertEquals(oldY + 1, session.curY);
        assertEquals(Tetrominoes.ZShape, session.shapeAt(0, 1));
        assertEquals(Tetrominoes.NoShape, session.shapeAt(3, 0));
        assertEquals(Tetrominoes.SquareShape, session.shapeAt(4, 0));
    }

    @Test
    void risingLineFailsWhenTopRowIsOccupied() {
        GameSession session = new GameSession(0);
        session.cells[(GameSession.HEIGHT - 1) * GameSession.WIDTH] = Tetrominoes.ZShape;
        assertFalse(session.raiseGarbageLine(0));
    }
}
