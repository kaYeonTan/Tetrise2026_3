package kr.ac.jbnu.se.tetris;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Random;
import javax.swing.JLabel;
import kr.ac.jbnu.se.tetris.boss.Blueprint;
import kr.ac.jbnu.se.tetris.core.GameSession;
import kr.ac.jbnu.se.tetris.integration.CoopEngineBridge;
import kr.ac.jbnu.se.tetris.persistence.CoopGameMemento;
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
        set.add(p.players[0].type);
        for (int i = 0; i < 6; i++) {
            set.add(p.players[0].next.get(i));
        }
        assertEquals(7, set.size());
    }

    @Test
    void singlePlayerGravityUsesTwoPlayerStageSpeed() {
        Board board = new Board(new JLabel());
        int[] expectedDelays = {1000, 850, 700, 550, 400};

        for (int stage = 1; stage <= expectedDelays.length; stage++) {
            int delay = GamePanel.baseDropIntervalMillis(stage);
            board.setBaseGravityDelay(delay);
            assertEquals(expectedDelays[stage - 1], board.timer.getDelay());
        }

        board.setBaseGravityDelay(GamePanel.baseDropIntervalMillis(4));
        board.setGravityMultiplier(3);
        assertEquals(550 / 3, board.timer.getDelay());
    }

    @Test
    void tetrisDamage() {
        for (int c = 0; c < 9; c++) {
            for (int r = 20; r < 24; r++) {
                p.players[0].board[r][c] = 8;
            }
        }
        p.players[0].type = 0;
        p.players[0].rot = 1;
        p.players[0].x = 7;
        p.players[0].y = 3;
        p.hardDrop(0);
        assertEquals(4, p.players[0].lines);
        assertEquals(40, p.bossHp);
    }

    @Test
    void wallKick() {
        p.players[0].type = 2;
        p.players[0].rot = 1;
        p.players[0].x = -1;
        p.players[0].y = 10;
        p.rotate(0, 1);
        assertEquals(2, p.players[0].rot);
        assertTrue(p.players[0].x >= 0);
    }

    @Test
    void holdOnce() {
        int first = p.players[0].type;
        p.hold(0);
        p.hold(0);
        assertEquals(first, p.players[0].hold);
    }

    @Test
    void twoPlayerDoublesBossMax() {
        p.newGame(false);
        p.stage = 1;
        p.startStage();
        assertEquals(100, p.bossMax);
        p.newGame(true);
        p.stage = 1;
        p.startStage();
        assertEquals(200, p.bossMax);
    }

    @Test
    void saveLoad() {
        p.newGame(true);
        p.loginUser = "tester";
        p.players[0].score = 500;
        p.players[1].board[23][3] = 5;
        assertTrue(FileManager.saveGame(p));
        GamePanel q = new GamePanel();
        q.timer.stop();
        assertTrue(FileManager.loadGame(p.saveFile, q));
        assertEquals(500, q.players[0].score);
        assertEquals(5, q.players[1].board[23][3]);
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
        int p1Y = p.players[0].y;
        int p2Y = p.players[1].y;
        p.raiseBlocks();
        int p1GarbageBlocks = 0;
        int p2GarbageBlocks = 0;
        for (int c = 0; c < 10; c++) {
            if (p.players[0].board[23][c] != 0) p1GarbageBlocks++;
            if (p.players[1].board[23][c] != 0) p2GarbageBlocks++;
        }
        assertEquals(9, p1GarbageBlocks);
        assertEquals(9, p2GarbageBlocks);
        assertEquals(p1Y - 1, p.players[0].y);
        assertEquals(p2Y - 1, p.players[1].y);
    }

    @Test
    void onlineHostTicksRisingPatternAndRaisesBothAuthoritativeBoards() {
        p.newGame(true);
        p.isOnlineCoop = true;
        p.localPlayerIndex = 0;
        p.stage = 2;
        p.startStage();
        p.coopEngineBridge = new CoopEngineBridge(p);
        p.coopEngineBridge.startOnlineHost(2, 7L);
        p.coopEngineBridge.getEngine().engine(0).softDropOnce();
        p.coopEngineBridge.getEngine().engine(1).softDropOnce();
        p.coopEngineBridge.getEngine().engine(0).pause();
        p.coopEngineBridge.getEngine().engine(1).pause();

        p.update();
        assertEquals(16, p.bossTimer);

        GameSession player0 = p.coopEngineBridge.session(0);
        GameSession player1 = p.coopEngineBridge.session(1);
        int player0Y = player0.curY;
        int player1Y = player1.curY;
        p.raiseBlocks();

        assertEquals(9, occupiedCells(player0, 0));
        assertEquals(9, occupiedCells(player1, 0));
        assertEquals(player0Y + 1, player0.curY);
        assertEquals(player1Y + 1, player1.curY);
    }

    @Test
    void onlineRandomKeyMappingUsesEachClientsSinglePlayerKeys() {
        int[] savedSingleKeys = GamePanel.keySingle;
        try {
            int[] hostSingleKeys = {101, 102, 103, 104, 105, 106, 107};
            int[] hostMapping = {1, 2, 3, 4, 5, 6, 0};
            int[] guestMapping = {6, 5, 4, 3, 2, 1, 0};
            GamePanel.keySingle = hostSingleKeys;
            p.newGame(true);
            p.isOnlineCoop = true;
            p.localPlayerIndex = 0;
            p.stage = 5;
            p.startStage();
            p.applyTemporaryKeyMappings(hostMapping, guestMapping, 10_000);

            assertArrayEquals(new int[]{102, 103, 104, 105, 106, 107, 101}, p.currentPrimaryKeys());

            CoopGameMemento sync = new CoopGameMemento();
            sync.stage = 5;
            sync.blueprintIndex = -1;
            sync.gameTime = p.gameTime;
            sync.bossTimer = p.bossTimer;
            p.fillOnlineBossSyncFields(sync);
            assertArrayEquals(hostMapping, sync.randomKeyMappingForPlayer0);
            assertArrayEquals(guestMapping, sync.randomKeyMappingForPlayer1);

            int[] guestSingleKeys = {201, 202, 203, 204, 205, 206, 207};
            GamePanel.keySingle = guestSingleKeys;
            p.localPlayerIndex = 1;
            p.applyOnlineBossSync(sync);

            assertArrayEquals(new int[]{207, 206, 205, 204, 203, 202, 201}, p.currentPrimaryKeys());
        } finally {
            GamePanel.keySingle = savedSingleKeys;
        }
    }

    @Test
    void stageFiveRandomKeyGuideRendersAboveBoss() {
        p.stage = 5;
        p.startStage();
        p.applyTemporaryKeyMappings(new int[]{1, 2, 3, 4, 5, 6, 0}, null, 10_000);
        BufferedImage image = new BufferedImage(1280, 720, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            p.drawRandomKeyGuide(graphics, 1100, 592);
        } finally {
            graphics.dispose();
        }

        assertTrue((image.getRGB(970, 398) >>> 24) > 0);
    }

    private static int occupiedCells(GameSession session, int y) {
        int occupied = 0;
        for (int x = 0; x < GameSession.WIDTH; x++) {
            if (session.shapeAt(x, y) != Tetrominoes.NoShape) {
                occupied++;
            }
        }
        return occupied;
    }

    @Test
    void twoPlayerStageThreeBagCanContainPentomino() {
        p.newGame(true);
        p.stage = 3;
        p.random = new Random(7);
        boolean found = false;
        for (int n = 0; n < 100 && !found; n++) {
            p.players[0].next.clear();
            p.players[0].bag.clear();
            p.fillNext(p.players[0].next, p.players[0].bag);
            for (int type : p.players[0].next) {
                if (type >= 7) found = true;
            }
            for (int type : p.players[0].bag) {
                if (type >= 7) found = true;
            }
        }
        assertTrue(found);
    }

    @Test
    void twoPlayerPentominoLocksFiveCells() {
        p.newGame(true);
        p.players[0].board = new int[24][10];
        p.players[0].type = 10;
        p.players[0].rot = 0;
        p.players[0].x = 3;
        p.players[0].y = 10;
        p.lock(0);
        int cells = 0;
        for (int[] row : p.players[0].board) {
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
                    int[][] board = (row + column) % 2 == 0 ? p.players[0].board : p.players[1].board;
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
        p.players[0].board[18 + forbiddenRow][forbiddenColumn] = 1;
        p.notifyBossBoardChanged();
        assertFalse(p.stageClear);

        p.players[0].board[18 + forbiddenRow][forbiddenColumn] = 0;
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

        p.players[1].board[18 + targetRow][targetColumn] = 1;
        assertTrue(p.isBlueprintCellFilledByOtherPlayer(0, targetRow, targetColumn));
        assertFalse(p.isBlueprintCellFilledByOtherPlayer(1, targetRow, targetColumn));

        p.players[0].board[18 + targetRow][targetColumn] = 1;
        assertFalse(p.isBlueprintCellFilledByOtherPlayer(0, targetRow, targetColumn));
    }
}
