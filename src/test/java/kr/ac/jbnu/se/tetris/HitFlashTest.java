package kr.ac.jbnu.se.tetris;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HitFlashTest {

    @Test
    void flashBlinksTwoOnTwoOff() {
        assertFalse(HitFlash.flashVisible(0));
        assertTrue(HitFlash.flashVisible(1));
        assertFalse(HitFlash.flashVisible(2));
        assertFalse(HitFlash.flashVisible(3));
        assertTrue(HitFlash.flashVisible(4));
        assertTrue(HitFlash.flashVisible(5));
        assertFalse(HitFlash.flashVisible(6));
    }

    @Test
    void shakeAlternatesAndStopsAtZero() {
        assertEquals(0, HitFlash.shakeOffset(0));
        assertEquals(3, HitFlash.shakeOffset(2));
        assertEquals(-3, HitFlash.shakeOffset(1));
    }
}
