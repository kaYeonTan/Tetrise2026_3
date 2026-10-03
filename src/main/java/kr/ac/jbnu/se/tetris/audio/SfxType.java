package kr.ac.jbnu.se.tetris.audio;

public enum SfxType {
    BUTTON_CLICK("/sound/press_button.mp3"),
    LINE_CLEAR_SINGLE("/sound/line_clear1.mp3"),
    LINE_CLEAR_MULTI("/sound/line_clear2.mp3"),
    BLOCK_LOCK("/sound/turn_key1.mp3"),
    STAGE_2_PATTERN("/sound/dummy_line.mp3"),
    STAGE_4_PATTERN("/sound/blizzard.mp3"),
    STAGE_5_PATTERN("/sound/confusion.mp3"),
    STAGE_CLEAR("/sound/level_clear.mp3");

    private final String resourcePath;

    SfxType(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String resourcePath() {
        return resourcePath;
    }
}
