package kr.ac.jbnu.se.tetris.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import org.junit.jupiter.api.Test;

class BossPatternControllerTest {

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
        assertEquals(4, target.gravityMultiplier);
        assertEquals(10, target.nextPieceMultiplier);
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
    }
}
