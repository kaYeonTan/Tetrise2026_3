package kr.ac.jbnu.se.tetris.audio;

public enum BgmType {
    BATTLE("/sound/Decktonic - BGM.mp3");

    private final String resourcePath;

    BgmType(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String resourcePath() {
        return resourcePath;
    }
}
