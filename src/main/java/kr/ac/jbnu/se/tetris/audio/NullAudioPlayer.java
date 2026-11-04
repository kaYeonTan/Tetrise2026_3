package kr.ac.jbnu.se.tetris.audio;

/** Silent AudioPlayer for tests and headless runs. */
public final class NullAudioPlayer implements AudioPlayer {

    private boolean muted;

    @Override
    public void playBgm(BgmType type) {
        // intentionally silent
    }

    @Override
    public void pauseBgm() {
        // intentionally silent
    }

    @Override
    public void resumeBgm() {
        // intentionally silent
    }

    @Override
    public void stopBgm() {
        // intentionally silent
    }

    @Override
    public void playSfx(SfxType type) {
        // intentionally silent
    }

    @Override
    public void playManagedSfx(SfxType type) {
        // intentionally silent
    }

    @Override
    public void stopManagedSfx(SfxType type) {
        // intentionally silent
    }

    @Override
    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    @Override
    public boolean isMuted() {
        return muted;
    }
}
