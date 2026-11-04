package kr.ac.jbnu.se.tetris.audio;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class SoundManagerTest {

    @Test
    void preloadDecodesAllBundledSounds() {
        try (SoundManager soundManager = new SoundManager()) {
            assertDoesNotThrow(soundManager::preload);
        }
    }
}
