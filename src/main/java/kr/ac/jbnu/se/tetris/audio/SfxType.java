package kr.ac.jbnu.se.tetris.audio;

public enum SfxType {
    BUTTON_CLICK("/sound/press_button.mp3"),
    LINE_CLEAR_SINGLE("/sound/line_clear1.mp3"),
    LINE_CLEAR_MULTI("/sound/line_clear2.mp3"),
    BLOCK_LOCK("/sound/turn_key1.mp3"),
    STAGE_2_PATTERN("/sound/dummy_line.mp3"),
    STAGE_4_PATTERN("/sound/blizzard.mp3", true),
    STAGE_5_PATTERN("/sound/confusion.mp3"),
    STAGE_CLEAR("/sound/level_clear.mp3");

    private final String resourcePath;
    private final boolean managedPlayback;

    SfxType(String resourcePath) {
        this(resourcePath, false);
    }

    SfxType(String resourcePath, boolean managedPlayback) {
        this.resourcePath = resourcePath;
        this.managedPlayback = managedPlayback;
    }

    public String resourcePath() {
        return resourcePath;
    }

    public boolean usesManagedPlayback() {
        return managedPlayback;
    }
}
