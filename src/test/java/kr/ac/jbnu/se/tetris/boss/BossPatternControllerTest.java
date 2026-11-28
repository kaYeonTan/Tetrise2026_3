package kr.ac.jbnu.se.tetris.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import kr.ac.jbnu.se.tetris.audio.SfxType;
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
    void stageTwoWarnsThreeSecondsBeforeRaisingBlocks() {
        FakeTarget target = new FakeTarget();
        target.garbageHole = 6;
        BossPatternController controller = new BossPatternController(new Random(1));
        controller.startStage(2, target);
        target.events.clear();

        controller.tick(16_999, target);
        assertFalse(target.warning);
        controller.tick(1, target);
        assertTrue(target.warning);
        assertTrue(target.events.isEmpty());
        controller.tick(3_000, target);

        assertEquals(List.of("clear warning", "STAGE_2_PATTERN", "raise blocks"), target.events);
        assertFalse(SfxType.STAGE_2_PATTERN.usesManagedPlayback());
        assertEquals(1, target.raised);
        assertEquals(6, target.raisedHole);
        assertFalse(target.warning);
        assertEquals(0, controller.elapsedMillis());
    }

    @Test
    void risingBlockPatternCanAttackWithOnlyItsOwnTarget() {
        List<String> events = new ArrayList<>();
        RisingBlockTarget target = new RisingBlockTarget() {
            @Override
            public int selectGarbageHole() {
                return 4;
            }

            @Override
            public void raiseBlocks(int hole) {
                events.add("raise at " + hole);
            }

            @Override
            public void showBossMessage(String message) {
                events.add("show message");
            }
        };

        new RisingBlockPattern(target).attack();

        assertEquals(List.of("raise at 4", "show message"), events);
    }

    @Test
    void stageChangeClearsWarningAndUsesNewPattern() {
        FakeTarget target = new FakeTarget();
        BossPatternController controller = new BossPatternController(new Random(1));
        controller.startStage(2, target);
        controller.tick(17_000, target);
        assertTrue(target.warning);

        controller.startStage(4, target);

        assertFalse(target.warning);
        assertEquals(0, controller.elapsedMillis());
        assertEquals(2, target.effectResets);
        target.events.clear();
        controller.tick(30_000, target);
        assertEquals(List.of("clear warning", "STAGE_4_PATTERN"), target.events);
        assertEquals(5_000, target.accelerationDuration);
        assertEquals(0, target.raised);
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
        target.events.clear();
        controller.tick(30_000, target);
        assertEquals(List.of("clear warning", "STAGE_4_PATTERN"), target.events);
        assertTrue(SfxType.STAGE_4_PATTERN.usesManagedPlayback());
        assertEquals(5_000, target.accelerationDuration);
        assertEquals(5, target.gravityMultiplier);
        assertEquals(10, target.nextPieceMultiplier);
    }

    @Test
    void stageThreeCompletesOnlyWhenTargetsAreFilledWithoutViolations() {
        FakeTarget target = new FakeTarget();
        BossPatternController controller = new BossPatternController(new Random(1));
        controller.startStage(3, target);
        target.events.clear();
        controller.tick(60_000, target);
        assertTrue(target.events.isEmpty());
        Blueprint blueprint = controller.blueprint();

        for (int row = 0; row < Blueprint.ROWS; row++) {
            for (int column = 0; column < Blueprint.COLUMNS; column++) {
                target.occupied[row][column] = blueprint.isTarget(row, column);
            }
        }
        int[] forbiddenCell = findForbiddenCell(blueprint);
        target.occupied[forbiddenCell[0]][forbiddenCell[1]] = true;
        controller.onBoardChanged();
        assertFalse(target.blueprintCompleted);
        assertEquals(1, controller.violationCount());

        target.occupied[forbiddenCell[0]][forbiddenCell[1]] = false;
        controller.onBoardChanged();
        assertTrue(target.blueprintCompleted);
        assertEquals(1.0, controller.progress());
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
        target.events.clear();
        controller.tick(20_000, target);
        assertEquals(List.of("clear warning", "STAGE_5_PATTERN"), target.events);
        assertFalse(SfxType.STAGE_5_PATTERN.usesManagedPlayback());
        assertEquals(10_000, target.keyDuration);
        int[] identity = {0, 1, 2, 3, 4, 5, 6};
        assertFalse(java.util.Arrays.equals(identity, target.primaryMapping));
        assertFalse(java.util.Arrays.equals(identity, target.secondaryMapping));
    }

    private static final class FakeTarget implements BossPatternTarget {
        final List<String> events = new ArrayList<>();
        boolean warning;
        int raised;
        int garbageHole;
        int raisedHole = -1;
        boolean twoPlayer;
        int accelerationDuration;
        int gravityMultiplier;
        int nextPieceMultiplier;
        int[] primaryMapping;
        int[] secondaryMapping;
        int keyDuration;
        int effectResets;
        boolean[][] occupied = new boolean[Blueprint.ROWS][Blueprint.COLUMNS];
        boolean blueprintCompleted;

        @Override
        public void showBossWarning(String message) {
            warning = true;
        }

        @Override
        public void clearBossWarning() {
            warning = false;
            events.add("clear warning");
        }

        @Override
        public void onBossPatternActivated(SfxType sound) {
            events.add(sound.name());
        }

        @Override
        public void showBossMessage(String message) {
            //보스 패턴 동작 확인용 메서드
        }

        @Override
        public void raiseBlocks() {
            raised++;
        }

        @Override
        public void raiseBlocks(int hole) {
            events.add("raise blocks");
            raised++;
            raisedHole = hole;
        }

        @Override
        public int selectGarbageHole() {
            return garbageHole;
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
        public void applyTemporaryKeyMappings(int[] primaryMapping, int[] secondaryMapping, int durationMillis) {
            this.primaryMapping = primaryMapping;
            this.secondaryMapping = secondaryMapping;
            keyDuration = durationMillis;
        }

        @Override
        public void resetTemporaryBossEffects() {
            effectResets++;
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
