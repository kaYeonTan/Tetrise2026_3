package kr.ac.jbnu.se.tetris.audio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/** MP3 음원을 PCM으로 디코딩해 BGM과 효과음을 재생한다. */
public final class SoundManager implements AudioPlayer, AutoCloseable {

    private final Map<BgmType, AudioData> bgmData = new EnumMap<>(BgmType.class);
    private final Map<SfxType, AudioData> sfxData = new EnumMap<>(SfxType.class);
    private final Map<SfxType, Clip> managedEffects = new EnumMap<>(SfxType.class);
    private final Set<Clip> activeEffects = Collections.synchronizedSet(new HashSet<>());

    private Clip bgmClip;
    private BgmType currentBgm;
    private int pausedFrame;
    private boolean paused;
    private volatile boolean muted;
    private boolean preloaded;

    public synchronized void preload() {
        if (preloaded) {
            return;
        }
        preloaded = true;
        for (BgmType type : BgmType.values()) {
            load(type.resourcePath(), type, bgmData);
        }
        for (SfxType type : SfxType.values()) {
            load(type.resourcePath(), type, sfxData);
        }
    }

    public synchronized void playBgm(BgmType type) {
        if (type == null) {
            return;
        }
        if (currentBgm != type) {
            closeBgmClip();
            currentBgm = type;
            pausedFrame = 0;
        }
        paused = false;
        if (muted || (bgmClip != null && bgmClip.isRunning())) {
            return;
        }
        startCurrentBgm();
    }

    public synchronized void pauseBgm() {
        if (bgmClip != null && bgmClip.isOpen()) {
            pausedFrame = bgmClip.getFramePosition();
            bgmClip.stop();
        }
        for (Clip clip : managedEffects.values()) {
            if (clip.isRunning()) {
                clip.stop();
            }
        }
        paused = true;
    }

    public synchronized void resumeBgm() {
        if (!paused) {
            return;
        }
        paused = false;
        if (!muted) {
            startCurrentBgm();
            resumeManagedEffects();
        }
    }

    public synchronized void stopBgm() {
        closeBgmClip();
        currentBgm = null;
        pausedFrame = 0;
        paused = false;
    }

    public void playSfx(SfxType type) {
        if (type == null || muted) {
            return;
        }
        AudioData data = sfxData.get(type);
        if (data == null) {
            return;
        }
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(data.format(), data.bytes(), 0, data.bytes().length);
            activeEffects.add(clip);
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    activeEffects.remove(clip);
                    clip.close();
                }
            });
            clip.start();
        } catch (LineUnavailableException | IllegalArgumentException e) {
            logFailure(type.resourcePath(), e);
        }
    }

    /** 패턴 종료 시점까지 제어해야 하는 지속 효과음을 재생한다. */
    public synchronized void playManagedSfx(SfxType type) {
        if (type == null) {
            return;
        }
        stopManagedSfx(type);
        if (muted) {
            return;
        }
        AudioData data = sfxData.get(type);
        if (data == null) {
            return;
        }
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(data.format(), data.bytes(), 0, data.bytes().length);
            managedEffects.put(type, clip);
            clip.start();
        } catch (LineUnavailableException | IllegalArgumentException e) {
            logFailure(type.resourcePath(), e);
        }
    }

    public synchronized void stopManagedSfx(SfxType type) {
        Clip clip = managedEffects.remove(type);
        if (clip != null) {
            clip.stop();
            clip.close();
        }
    }

    public synchronized void setMuted(boolean muted) {
        if (this.muted == muted) {
            return;
        }
        this.muted = muted;
        if (muted) {
            if (bgmClip != null && bgmClip.isOpen()) {
                pausedFrame = bgmClip.getFramePosition();
                bgmClip.stop();
            }
            for (Clip clip : managedEffects.values()) {
                if (clip.isRunning()) {
                    clip.stop();
                }
            }
        } else if (!paused) {
            startCurrentBgm();
            resumeManagedEffects();
        }
    }

    public synchronized boolean isMuted() {
        return muted;
    }

    private void startCurrentBgm() {
        if (currentBgm == null) {
            return;
        }
        AudioData data = bgmData.get(currentBgm);
        if (data == null) {
            return;
        }
        try {
            if (bgmClip == null || !bgmClip.isOpen()) {
                bgmClip = AudioSystem.getClip();
                bgmClip.open(data.format(), data.bytes(), 0, data.bytes().length);
            }
            int frame = Math.min(pausedFrame, Math.max(0, bgmClip.getFrameLength() - 1));
            bgmClip.setFramePosition(frame);
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (LineUnavailableException | IllegalArgumentException e) {
            logFailure(currentBgm.resourcePath(), e);
            closeBgmClip();
        }
    }

    private void closeBgmClip() {
        if (bgmClip != null) {
            bgmClip.stop();
            bgmClip.close();
            bgmClip = null;
        }
    }

    private void resumeManagedEffects() {
        for (Clip clip : managedEffects.values()) {
            if (clip.isOpen() && clip.getFramePosition() < clip.getFrameLength()) {
                clip.start();
            }
        }
    }

    private <T extends Enum<T>> void load(String path, T type, Map<T, AudioData> target) {
        URL resource = SoundManager.class.getResource(path);
        if (resource == null) {
            System.err.println("Sound file not found: " + path);
            return;
        }
        try (AudioInputStream source = AudioSystem.getAudioInputStream(resource)) {
            AudioFormat base = source.getFormat();
            int channels = base.getChannels();
            AudioFormat decodedFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    base.getSampleRate(),
                    16,
                    channels,
                    channels * 2,
                    base.getSampleRate(),
                    false);
            try (AudioInputStream decoded = AudioSystem.getAudioInputStream(decodedFormat, source)) {
                target.put(type, new AudioData(decodedFormat, readAll(decoded)));
            }
        } catch (UnsupportedAudioFileException | IOException | IllegalArgumentException e) {
            logFailure(path, e);
        }
    }

    private byte[] readAll(AudioInputStream stream) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[16 * 1024];
        int read;
        while ((read = stream.read(buffer)) >= 0) {
            if (read > 0) {
                output.write(buffer, 0, read);
            }
        }
        return output.toByteArray();
    }

    private void logFailure(String path, Exception e) {
        System.err.println("Sound load/play failed: " + path + " (" + e.getMessage() + ")");
    }

    @Override
    public synchronized void close() {
        stopBgm();
        for (Clip clip : managedEffects.values()) {
            clip.stop();
            clip.close();
        }
        managedEffects.clear();
        synchronized (activeEffects) {
            for (Clip clip : activeEffects) {
                clip.stop();
                clip.close();
            }
            activeEffects.clear();
        }
        bgmData.clear();
        sfxData.clear();
    }

    private record AudioData(AudioFormat format, byte[] bytes) {
    }
}
