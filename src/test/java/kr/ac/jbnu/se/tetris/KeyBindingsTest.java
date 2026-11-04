package kr.ac.jbnu.se.tetris;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.KeyEvent;
import org.junit.jupiter.api.Test;

class KeyBindingsTest {

    @Test
    void copyIsIndependent() {
        KeyBindings a = KeyBindings.defaults();
        KeyBindings b = a.copy();
        assertTrue(a.sameAs(b));
        b.set(KeyBindings.SINGLE, 0, KeyEvent.VK_X);
        assertFalse(a.sameAs(b));
        assertEquals(KeyEvent.VK_LEFT, a.get(KeyBindings.SINGLE, 0));
    }

    @Test
    void conflictInSameTab() {
        KeyBindings k = KeyBindings.defaults();
        assertEquals("1인 모드 · 오른쪽 이동", k.findConflict(KeyBindings.SINGLE, 0, KeyEvent.VK_RIGHT));
    }

    @Test
    void ownCurrentKeyIsNotAConflict() {
        KeyBindings k = KeyBindings.defaults();
        assertNull(k.findConflict(KeyBindings.SINGLE, 0, KeyEvent.VK_LEFT));
    }

    @Test
    void singleTabDoesNotConflictWithP1() {
        KeyBindings k = KeyBindings.defaults();
        assertNull(k.findConflict(KeyBindings.SINGLE, 0, KeyEvent.VK_A));
    }

    @Test
    void p1AndP2ConflictWithEachOther() {
        KeyBindings k = KeyBindings.defaults();
        assertEquals("2인 P2 · 왼쪽 이동", k.findConflict(KeyBindings.P1, 0, KeyEvent.VK_LEFT));
    }

    @Test
    void pauseKeyConflictsWithEveryTab() {
        KeyBindings k = KeyBindings.defaults();
        assertEquals("공통 · 일시정지", k.findConflict(KeyBindings.SINGLE, 0, KeyEvent.VK_P));
        assertEquals("1인 모드 · 왼쪽 이동", k.findConflict(KeyBindings.COMMON, 0, KeyEvent.VK_LEFT));
    }

    @Test
    void ignoresModifierKeys() {
        assertTrue(KeyBindings.isIgnoredKey(KeyEvent.VK_SHIFT));
        assertFalse(KeyBindings.isIgnoredKey(KeyEvent.VK_A));
    }

    @Test
    void rejectsUnknownTab() {
        assertThrows(IllegalArgumentException.class, () -> KeyBindings.defaultKeys(9));
    }
}
