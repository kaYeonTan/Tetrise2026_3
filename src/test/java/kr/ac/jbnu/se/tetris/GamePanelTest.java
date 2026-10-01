package kr.ac.jbnu.se.tetris;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Random;
import kr.ac.jbnu.se.tetris.boss.Blueprint;
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

    @Test
    void stageThreeSaveKeepsSelectedBlueprint() {
        p.loginUser = "tester";
        p.stage = 3;
        p.startStage();
        int selectedBlueprint = p.bossPattern.blueprintIndex();

        assertTrue(FileManager.saveGame(p));
        GamePanel loaded = new GamePanel();
        loaded.timer.stop();
        assertTrue(FileManager.loadGame(p.saveFile, loaded));
        assertEquals(selectedBlueprint, loaded.bossPattern.blueprintIndex());
        FileManager.deleteSave(p.saveFile);
    }

    @Test
    void risingPatternMovesBothTwoPlayerBoardsImmediately() {
        p.newGame(true);
        int p1Y = p.p1Y;
        int p2Y = p.p2Y;
        p.raiseBlocks();
        int p1GarbageBlocks = 0;
        int p2GarbageBlocks = 0;
        for (int c = 0; c < 10; c++) {
            if (p.board1[23][c] != 0) p1GarbageBlocks++;
            if (p.board2[23][c] != 0) p2GarbageBlocks++;
        }
        assertEquals(9, p1GarbageBlocks);
        assertEquals(9, p2GarbageBlocks);
        assertEquals(p1Y - 1, p.p1Y);
        assertEquals(p2Y - 1, p.p2Y);
    }

    @Test
    void twoPlayerStageThreeBagCanContainPentomino() {
        p.newGame(true);
        p.stage = 3;
        p.random = new Random(7);
        boolean found = false;
        for (int n = 0; n < 100 && !found; n++) {
            p.p1Next.clear();
            p.p1Bag.clear();
            p.fillNext(p.p1Next, p.p1Bag);
            for (int type : p.p1Next) {
                if (type >= 7) found = true;
            }
            for (int type : p.p1Bag) {
                if (type >= 7) found = true;
            }
        }
        assertTrue(found);
    }

    @Test
    void twoPlayerPentominoLocksFiveCells() {
        p.newGame(true);
        p.board1 = new int[24][10];
        p.p1Type = 10;
        p.p1Rot = 0;
        p.p1X = 3;
        p.p1Y = 10;
        p.lockP1();
        int cells = 0;
        for (int[] row : p.board1) {
            for (int cell : row) {
                if (cell == 9) cells++;
            }
        }
        assertEquals(5, cells);
    }

    @Test
    void twoPlayerStageThreeCombinesBoardsAndRejectsEmptyCellViolations() {
        p.newGame(true);
        p.stage = 3;
        p.startStage();
        Blueprint blueprint = p.bossPattern.blueprint();

        for (int row = 0; row < Blueprint.ROWS; row++) {
            for (int column = 0; column < Blueprint.COLUMNS; column++) {
                if (blueprint.isTarget(row, column)) {
                    int[][] board = (row + column) % 2 == 0 ? p.board1 : p.board2;
                    board[18 + row][column] = 1;
                }
            }
        }
        int forbiddenRow = -1;
        int forbiddenColumn = -1;
        for (int row = 0; row < Blueprint.ROWS && forbiddenRow < 0; row++) {
            for (int column = 0; column < Blueprint.COLUMNS; column++) {
                if (blueprint.mustBeEmpty(row, column)) {
                    forbiddenRow = row;
                    forbiddenColumn = column;
                    break;
                }
            }
        }
        p.board1[18 + forbiddenRow][forbiddenColumn] = 1;
        p.notifyBossBoardChanged();
        assertFalse(p.stageClear);

        p.board1[18 + forbiddenRow][forbiddenColumn] = 0;
        p.notifyBossBoardChanged();
        assertTrue(p.stageClear);
    }

    @Test
    void stageThreeMarksTargetFilledOnlyByOtherPlayer() {
        p.newGame(true);
        p.stage = 3;
        p.startStage();
        Blueprint blueprint = p.bossPattern.blueprint();
        int targetRow = -1;
        int targetColumn = -1;
        for (int row = 0; row < Blueprint.ROWS && targetRow < 0; row++) {
            for (int column = 0; column < Blueprint.COLUMNS; column++) {
                if (blueprint.isTarget(row, column)) {
                    targetRow = row;
                    targetColumn = column;
                    break;
                }
            }
        }

        p.board2[18 + targetRow][targetColumn] = 1;
        assertTrue(p.isBlueprintCellFilledByOtherPlayer(0, targetRow, targetColumn));
        assertFalse(p.isBlueprintCellFilledByOtherPlayer(1, targetRow, targetColumn));

        p.board1[18 + targetRow][targetColumn] = 1;
        assertFalse(p.isBlueprintCellFilledByOtherPlayer(0, targetRow, targetColumn));
    }
}
