package kr.ac.jbnu.se.tetris.audio;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import org.junit.jupiter.api.Test;

class SoundManagerTest {

    @Test
    void preloadDecodesAllBundledSounds() {
        try (SoundManager soundManager = new SoundManager()) {
            assertDoesNotThrow(soundManager::preload);
        }
    }

    @Test
    void closesAcquiredClipWhenOpeningFailsForEachPlaybackMode() {
        ClipProbe failed = new ClipProbe(true);
        try (SoundManager soundManager = new SoundManager(() -> failed.clip)) {
            soundManager.preload();
            soundManager.playSfx(SfxType.BUTTON_CLICK);
            soundManager.playManagedSfx(SfxType.STAGE_4_PATTERN);
            soundManager.playBgm(BgmType.BATTLE);
        }

        assertEquals(List.of("open", "close", "open", "close", "open", "close"), failed.calls);
    }

    @Test
    void ordinaryAndManagedEffectsCloseThroughTheirStopPaths() {
        ClipProbe ordinary = new ClipProbe(false);
        ClipProbe managed = new ClipProbe(false);
        try (SoundManager soundManager = new SoundManager(List.of(ordinary.clip, managed.clip).iterator()::next)) {
            soundManager.preload();
            soundManager.playSfx(SfxType.BUTTON_CLICK);
            ordinary.finish();
            soundManager.playManagedSfx(SfxType.STAGE_4_PATTERN);
            soundManager.stopManagedSfx(SfxType.STAGE_4_PATTERN);
        }

        assertEquals(List.of("open", "start", "close"), ordinary.calls);
        assertEquals(List.of("open", "start", "stop", "close"), managed.calls);
    }

    @Test
    void bgmReusesOpenClipAndLoopsFromPausedFrame() {
        ClipProbe bgm = new ClipProbe(false);
        try (SoundManager soundManager = new SoundManager(() -> bgm.clip)) {
            soundManager.preload();
            soundManager.playBgm(BgmType.BATTLE);
            bgm.frame = 42;
            soundManager.pauseBgm();
            soundManager.resumeBgm();
        }

        String loop = "loop " + Clip.LOOP_CONTINUOUSLY;
        assertEquals(List.of("open", "frame 0", loop, "stop", "frame 42", loop, "stop", "close"), bgm.calls);
    }

    private static final class ClipProbe {
        final List<String> calls = new ArrayList<>();
        final Clip clip;
        int frame;
        private boolean open;
        private LineListener listener;

        ClipProbe(boolean failOpen) {
            clip = (Clip) Proxy.newProxyInstance(Clip.class.getClassLoader(), new Class<?>[]{Clip.class},
                    (instance, method, args) -> {
                        switch (method.getName()) {
                            case "open" -> {
                                calls.add("open");
                                if (failOpen) {
                                    throw new LineUnavailableException("test open failure");
                                }
                                open = true;
                            }
                            case "close" -> {
                                calls.add("close");
                                open = false;
                            }
                            case "start", "stop" -> calls.add(method.getName());
                            case "addLineListener" -> listener = (LineListener) args[0];
                            case "setFramePosition" -> {
                                frame = (int) args[0];
                                calls.add("frame " + frame);
                            }
                            case "loop" -> calls.add("loop " + args[0]);
                            case "isOpen" -> { return open; }
                            case "getFramePosition" -> { return frame; }
                            case "getFrameLength" -> { return 100; }
                            case "hashCode" -> { return System.identityHashCode(instance); }
                            case "equals" -> { return instance == args[0]; }
                            default -> throw new AssertionError("Unexpected Clip call: " + method.getName());
                        }
                        return null;
                    });
        }

        void finish() {
            listener.update(new LineEvent(clip, LineEvent.Type.STOP, frame));
        }
    }
}
