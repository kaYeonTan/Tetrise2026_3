package kr.ac.jbnu.se.tetris.audio;

/** Audio abstraction so game code does not depend on a concrete sound library. */
public interface AudioPlayer {

    void playBgm(BgmType type);

    void pauseBgm();

    void resumeBgm();

    void stopBgm();

    void playSfx(SfxType type);

    void playManagedSfx(SfxType type);

    void stopManagedSfx(SfxType type);

    void setMuted(boolean muted);

    boolean isMuted();
}
