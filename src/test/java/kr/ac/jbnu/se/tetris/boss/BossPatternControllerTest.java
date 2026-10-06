package kr.ac.jbnu.se.tetris.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BossPatternControllerTest {

    @Test
    void blueprintRequiresTargetOnBottomRow() {
        assertThrows(IllegalArgumentException.class, () -> Blueprint.fromRows(
                "T--------E",
                "---------E",
                "---------E",
                "---------E",
                "---------E",
                "---------E"));
    }

    @Test
    void blueprintRequiresAllTargetsToBeConnected() {
        assertThrows(IllegalArgumentException.class, () -> Blueprint.fromRows(
                "T--------E",
                "---------E",
                "---------E",
                "---------E",
                "---------E",
                "E-------T-"));
    }

    @Test
    void stageThreeHasSixDistinctValidBlueprints() {
        assertEquals(6, Blueprint.stageThreePatternCount());
        Set<String> patterns = new HashSet<>();
        for (int index = 0; index < Blueprint.stageThreePatternCount(); index++) {
            Blueprint blueprint = Blueprint.stageThree(index);
            StringBuilder signature = new StringBuilder();
            boolean bottomHasTarget = false;
            for (int row = 0; row < Blueprint.ROWS; row++) {
                for (int column = 0; column < Blueprint.COLUMNS; column++) {
                    signature.append(blueprint.isTarget(row, column) ? 'T'
                            : blueprint.mustBeEmpty(row, column) ? 'E' : '-');
                    if (row == Blueprint.ROWS - 1 && blueprint.isTarget(row, column)) {
                        bottomHasTarget = true;
                    }
                }
            }
            assertTrue(bottomHasTarget);
            patterns.add(signature.toString());
        }
        assertEquals(Blueprint.stageThreePatternCount(), patterns.size());
    }

    @Test
    void stageThreeCanRestoreSelectedBlueprint() {
        BossPatternController controller = new BossPatternController(new Random(1));
        controller.startStage(3, new FakeTarget(), 5);

        assertEquals(5, controller.blueprintIndex());
    }

    @Test
    void stageThreeTwoPlayerUsesDifferentBlueprintIndices() {
        FakeTarget target = new FakeTarget();
        target.twoPlayer = true;
        BossPatternController controller = new BossPatternController(new Random(3));
        controller.startStage(3, target);
        assertNotEquals(controller.blueprintIndex(), controller.blueprintIndexP1());
        assertNotSame(controller.blueprintForPlayer(0), controller.blueprintForPlayer(1));
    }

    @Test
    void stageTwoWarnsThreeSecondsBeforeRaisingBlocks() {
        FakeTarget target = new FakeTarget();
        BossPatternController controller = new BossPatternController(new Random(1));
        controller.startStage(2, target);

        controller.tick(16_999, target);
        assertFalse(target.warning);
        controller.tick(1, target);
        assertTrue(target.warning);
        controller.tick(3_000, target);

        assertEquals(1, target.raised);
        assertFalse(target.warning);
        assertEquals(0, controller.elapsedMillis());
    }

    @Test
    void stagesWithoutTimedAttackDoNothing() {
        FakeTarget target = new FakeTarget();
        BossPatternController controller = new BossPatternController(new Random(1));
        controller.startStage(1, target);
        controller.tick(60_000, target);
        assertEquals(0, target.raised);
        assertFalse(target.warning);
    }

    @Test
    void stageFourStartsSpecifiedAccelerationAfterThirtySeconds() {
        FakeTarget target = new FakeTarget();
        BossPatternController controller = new BossPatternController(new Random(1));
        controller.startStage(4, target);
        controller.tick(30_000, target);
        assertEquals(5_000, target.accelerationDuration);
        assertEquals(3, target.gravityMultiplier);
        assertEquals(10, target.nextPieceMultiplier);
    }

    @Test
    void stageThreeCompletesOnlyWhenTargetsAreFilledWithoutViolations() {
        FakeTarget target = new FakeTarget();
        BossPatternController controller = new BossPatternController(new Random(1));
        controller.startStage(3, target);
        Blueprint blueprint = controller.blueprint();

        for (int row = 0; row < Blueprint.ROWS; row++) {
            for (int column = 0; column < Blueprint.COLUMNS; column++) {
                target.occupied[row][column] = blueprint.isTarget(row, column);
            }
        }
        int[] forbiddenCell = findForbiddenCell(blueprint);
        target.occupied[forbiddenCell[0]][forbiddenCell[1]] = true;
        controller.onBoardChanged(target);
        assertFalse(target.blueprintCompleted);
        assertEquals(1, controller.violationCount(target));

        target.occupied[forbiddenCell[0]][forbiddenCell[1]] = false;
        controller.onBoardChanged(target);
        assertTrue(target.blueprintCompleted);
        assertEquals(1.0, controller.progress(target));
    }

    private int[] findForbiddenCell(Blueprint blueprint) {
        for (int row = 0; row < Blueprint.ROWS; row++) {
            for (int column = 0; column < Blueprint.COLUMNS; column++) {
                if (blueprint.mustBeEmpty(row, column)) {
                    return new int[]{row, column};
                }
            }
        }
        throw new AssertionError("Blueprint needs at least one forbidden cell");
    }

    @Test
    void stageFiveAppliesIndependentTemporaryMappingsForTwoPlayers() {
        FakeTarget target = new FakeTarget();
        target.twoPlayer = true;
        BossPatternController controller = new BossPatternController(new Random(2));
        controller.startStage(5, target);
        controller.tick(20_000, target);
        assertEquals(10_000, target.keyDuration);
        assertFalse(java.util.Arrays.equals(target.primaryOriginalKeys(), target.primaryKeys));
        assertFalse(java.util.Arrays.equals(target.secondaryOriginalKeys(), target.secondaryKeys));
    }

    private static final class FakeTarget implements BossPatternTarget {
        boolean warning;
        int raised;
        boolean twoPlayer;
        int accelerationDuration;
        int gravityMultiplier;
        int nextPieceMultiplier;
        int[] primaryKeys;
        int[] secondaryKeys;
        int keyDuration;
        boolean[][] occupied = new boolean[Blueprint.ROWS][Blueprint.COLUMNS];
        boolean blueprintCompleted;

        @Override
        public void showBossWarning(String message) {
            warning = true;
        }

        @Override
        public void clearBossWarning() {
            warning = false;
        }

        @Override
        public void showBossMessage(String message) {
        }

        @Override
        public void raiseBlocks() {
            raised++;
        }

        @Override
        public void startAcceleration(int durationMillis, int gravityMultiplier, int nextPieceMultiplier) {
            accelerationDuration = durationMillis;
            this.gravityMultiplier = gravityMultiplier;
            this.nextPieceMultiplier = nextPieceMultiplier;
        }

        @Override
        public int[] primaryOriginalKeys() {
            return new int[]{1, 2, 3, 4, 5, 6, 7};
        }

        @Override
        public int[] secondaryOriginalKeys() {
            return new int[]{8, 9, 10, 11, 12, 13, 14};
        }

        @Override
        public boolean isTwoPlayerMode() {
            return twoPlayer;
        }

        @Override
        public void applyTemporaryKeys(int[] primaryKeys, int[] secondaryKeys, int durationMillis) {
            this.primaryKeys = primaryKeys;
            this.secondaryKeys = secondaryKeys;
            keyDuration = durationMillis;
        }

        @Override
        public void resetTemporaryBossEffects() {
        }

        @Override
        public boolean isBlueprintCellOccupied(int row, int column) {
            return occupied[row][column];
        }

        @Override
        public void completeBlueprintPattern() {
            blueprintCompleted = true;
        }
    }
}
