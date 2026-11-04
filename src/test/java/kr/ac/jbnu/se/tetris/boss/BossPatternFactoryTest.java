package kr.ac.jbnu.se.tetris.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;

class BossPatternFactoryTest {

    private final BossPatternFactory factory = new BossPatternFactory(new Random(1));

    @Test
    void createsPatternMatchingEachStage() {
        assertSame(NoBossPattern.INSTANCE, factory.create(1, BossPatternFactory.NO_BLUEPRINT));
        assertInstanceOf(RisingBlockPattern.class, factory.create(2, BossPatternFactory.NO_BLUEPRINT));
        assertInstanceOf(BlueprintPattern.class, factory.create(3, 0));
        assertInstanceOf(AccelerationPattern.class, factory.create(4, BossPatternFactory.NO_BLUEPRINT));
        assertInstanceOf(RandomKeyPattern.class, factory.create(5, BossPatternFactory.NO_BLUEPRINT));
    }

    @Test
    void blueprintIndexIsOnlyUsedForStageThree() {
        assertEquals(BossPatternFactory.NO_BLUEPRINT, factory.resolveBlueprintIndex(2, 1));
        assertEquals(1, factory.resolveBlueprintIndex(3, 1));
        int random = factory.resolveBlueprintIndex(3, -1);
        assertTrue(random >= 0 && random < Blueprint.stageThreePatternCount());
    }
}
