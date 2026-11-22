package kr.ac.jbnu.se.tetris;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import kr.ac.jbnu.se.tetris.boss.Blueprint;
import kr.ac.jbnu.se.tetris.repository.Repositories;
import kr.ac.jbnu.se.tetris.core.GameSession;
import kr.ac.jbnu.se.tetris.input.KeyAction;
import kr.ac.jbnu.se.tetris.integration.CoopEngineBridge;
import kr.ac.jbnu.se.tetris.persistence.CoopGameMemento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GamePanelTest {

    GamePanel p;
    Repositories repos;

    @BeforeEach
    void setUp() {
        repos = Repositories.at(Path.of("target/testdata"));
        p = new GamePanel(repos);
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
    void baseGravityDelayUsesStageAndPlayTimeWithAMinimum() {
        int[] expectedDelays = {2_000, 1_850, 1_700, 1_550, 1_400};

        for (int stage = 1; stage <= expectedDelays.length; stage++) {
            p.stage = stage;
            assertEquals(expectedDelays[stage - 1], p.baseDropIntervalMillis());
        }

        p.gameTime = 60_000;
        assertEquals(1_200, p.baseDropIntervalMillis());
        p.gameTime = Long.MAX_VALUE;
        assertEquals(400, p.baseDropIntervalMillis());
    }

    @Test
    void singlePlayerUpdatesTheTimerAndAppliesBossAcceleration() throws ReflectiveOperationException {
        Board board = singlePlayerBoard(p);
        board.timer.stop();
        assertEquals(2_000, board.timer.getDelay());

        p.update(29_999);
        assertEquals(2_000, board.timer.getDelay());
        p.update(1);
        assertEquals(1_900, board.timer.getDelay());

        p.stage = 4;
        p.startStage();
        board.timer.stop();
        assertEquals(1_450, board.timer.getDelay());
        p.gameTime = 59_999;
        p.restoreBossPatternTimer(29_999);
        p.update(1);
        assertEquals(1_350 / 5, board.timer.getDelay());
        p.update(5_000);
        assertEquals(1_350, board.timer.getDelay());
    }

    @Test
    void localCoopGravityUsesElapsedTimeForBothPlayers() {
        p.newGame(true);
        int initialP1Y = p.p1Y;
        int initialP2Y = p.p2Y;

        for (int frame = 0; frame < 62; frame++) {
            p.update(32);
        }
        assertEquals(initialP1Y, p.p1Y);
        assertEquals(initialP2Y, p.p2Y);

        p.update(24);
        assertEquals(initialP1Y + 1, p.p1Y);
        assertEquals(initialP2Y + 1, p.p2Y);

        p.update(1_992);
        assertEquals(initialP1Y + 2, p.p1Y);
        assertEquals(initialP2Y + 2, p.p2Y);

        p.gameTime = 30_000;
        p.update(1_899);
        assertEquals(initialP1Y + 2, p.p1Y);
        assertEquals(initialP2Y + 2, p.p2Y);
        p.update(1);
        assertEquals(initialP1Y + 3, p.p1Y);
        assertEquals(initialP2Y + 3, p.p2Y);
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
        p.setLoginUser("tester");
        p.p1Score = 500;
        p.board2[23][3] = 5;
        assertTrue(repos.saves().saveGame(p));
        GamePanel q = new GamePanel(repos);
        q.timer.stop();
        assertTrue(repos.saves().loadGame(p.saveFile, q));
        assertEquals(500, q.p1Score);
        assertEquals(5, q.board2[23][3]);
        assertTrue(q.isTwoPlayer);
        repos.saves().deleteSave(p.saveFile);
    }

    @Test
    void stageThreeSaveKeepsSelectedBlueprintAndPlayTime() throws ReflectiveOperationException {
        p.setLoginUser("tester");
        p.stage = 3;
        p.startStage();
        p.gameTime = 60_000;
        int selectedBlueprint = p.bossPattern.blueprintIndex();

        assertTrue(repos.saves().saveGame(p));
        GamePanel loaded = new GamePanel(repos);
        loaded.timer.stop();
        assertTrue(repos.saves().loadGame(p.saveFile, loaded));
        assertEquals(selectedBlueprint, loaded.bossPattern.blueprintIndex());
        Board loadedBoard = singlePlayerBoard(loaded);
        loadedBoard.timer.stop();
        assertEquals(1_500, loadedBoard.timer.getInitialDelay());
        repos.saves().deleteSave(p.saveFile);
    }

    private static Board singlePlayerBoard(GamePanel panel) throws ReflectiveOperationException {
        var boardField = panel.aEngineBridge.getClass().getDeclaredField("board");
        boardField.setAccessible(true);
        return (Board) boardField.get(panel.aEngineBridge);
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
    void risingPatternLeavesBothBoardsUnchangedWhenFirstPlayerTopRowIsOccupied() {
        p.newGame(true);
        p.board1[0][4] = 3;
        int[][] expectedBoard1 = new int[24][10];
        expectedBoard1[0][4] = 3;
        int p1Y = p.p1Y;
        int p2Y = p.p2Y;

        p.raiseBlocks(2);

        assertEquals(GamePanel.SCREEN_RESULT, p.getScreen());
        assertArrayEquals(expectedBoard1, p.board1);
        assertArrayEquals(new int[24][10], p.board2);
        assertEquals(p1Y, p.p1Y);
        assertEquals(p2Y, p.p2Y);
    }

    @Test
    void risingPatternKeepsFirstPlayerShiftWhenSecondPlayerPieceWouldCrossTop() {
        p.newGame(true);
        p.p1Y = 3;
        p.p2Type = 1;
        p.p2Rot = 0;
        p.p2Y = 0;
        p.board1[1][0] = 3;
        p.board2[1][0] = 4;
        int[][] expectedBoard1 = new int[24][10];
        expectedBoard1[0][0] = 3;
        Arrays.fill(expectedBoard1[23], 8);
        expectedBoard1[23][2] = 0;
        int[][] expectedBoard2 = new int[24][10];
        expectedBoard2[1][0] = 4;

        p.raiseBlocks(2);

        assertEquals(GamePanel.SCREEN_RESULT, p.getScreen());
        assertArrayEquals(expectedBoard1, p.board1);
        assertArrayEquals(expectedBoard2, p.board2);
        assertEquals(2, p.p1Y);
        assertEquals(0, p.p2Y);
    }

    @Test
    void addGarbageSkipsRandomHoleWhenBlockedAndShiftsRowsOnSuccess() {
        p.random.setSeed(7L);
        Random expectedRandom = new Random(7L);
        int[][] board = new int[24][10];
        board[0][4] = 3;
        board[23][5] = 6;
        int[][] expected = new int[24][10];
        expected[0][4] = 3;
        expected[23][5] = 6;

        assertFalse(p.addGarbage(board));
        assertArrayEquals(expected, board);

        board[0][4] = 0;
        assertTrue(p.addGarbage(board));
        expected[0][4] = 0;
        expected[22][5] = 6;
        Arrays.fill(expected[23], 8);
        expected[23][expectedRandom.nextInt(10)] = 0;
        assertArrayEquals(expected, board);
        assertEquals(expectedRandom.nextLong(), p.random.nextLong());
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

        p.update(31);
        assertEquals(31, p.bossTimer);

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
    void onlinePlayersUseRealTicksAndTheSharedPlayTimeForGravity() {
        p.beginOnlineCoop(0, "gravity-test", 7L);
        GamePanel guest = new GamePanel(repos);
        guest.timer.stop();
        guest.beginOnlineCoop(1, "gravity-test", 7L);
        GameSession hostBoard = p.coopEngineBridge.session(0);
        GameSession guestBoard = guest.coopEngineBridge.session(1);
        int hostY = hostBoard.curY;
        int guestY = guestBoard.curY;

        p.update(1_999);
        guest.update(1_999);
        assertEquals(hostY, hostBoard.curY);
        assertEquals(guestY, guestBoard.curY);
        p.update(1);
        guest.update(1);
        assertEquals(hostY - 1, hostBoard.curY);
        assertEquals(guestY - 1, guestBoard.curY);

        p.gameTime = 29_999;
        p.update(1);
        CoopGameMemento snapshot = p.coopEngineBridge.createMemento("gravity-test");
        guest.coopEngineBridge.syncGuestFromMemento(snapshot);
        assertEquals(30_000, guest.gameTime);
        int syncedGuestY = guestBoard.curY;
        guest.update(1_899);
        assertEquals(syncedGuestY, guestBoard.curY);
        guest.update(1);
        p.update(1_899);
        assertEquals(syncedGuestY - 1, guestBoard.curY);
        assertEquals(hostY - 2, hostBoard.curY);
    }

    @Test
    void randomKeyPatternUsesElapsedTimeForActivationAndExpiry() {
        p.newGame(true);
        p.stage = 5;
        p.startStage();
        p.restoreBossPatternTimer(19_970);

        p.update(31);
        assertNotNull(p.temporaryPrimaryKeyMapping);
        assertEquals(10_031, p.randomKeyEnd);

        p.update(9_999);
        assertNotNull(p.temporaryPrimaryKeyMapping);
        p.update(1);
        assertNull(p.temporaryPrimaryKeyMapping);
    }

    @Test
    void onlineRandomKeyMappingUsesEachClientsSinglePlayerKeys() {
        kr.ac.jbnu.se.tetris.input.KeyBindings savedSingleKeys = GamePanel.keySingle().copy();
        try {
            int[] hostSingleKeys = {101, 102, 103, 104, 105, 106, 107};
            int[] hostMapping = {1, 2, 3, 4, 5, 6, 0};
            int[] guestMapping = {6, 5, 4, 3, 2, 1, 0};
            setSingleKeys(hostSingleKeys);
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
            setSingleKeys(guestSingleKeys);
            p.localPlayerIndex = 1;
            p.applyOnlineBossSync(sync);

            assertArrayEquals(new int[]{207, 206, 205, 204, 203, 202, 201}, p.currentPrimaryKeys());
        } finally {
            for (KeyAction action : KeyAction.values()) {
                GamePanel.keySingle().set(action, savedSingleKeys.get(action));
            }
        }
    }

    private static void setSingleKeys(int[] codes) {
        for (KeyAction action : KeyAction.values()) {
            GamePanel.keySingle().set(action, codes[action.ordinal()]);
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
