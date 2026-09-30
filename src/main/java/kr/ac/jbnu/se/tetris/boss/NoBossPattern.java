package kr.ac.jbnu.se.tetris.boss;

final class NoBossPattern implements BossPattern {

    static final NoBossPattern INSTANCE = new NoBossPattern();

    private NoBossPattern() {
    }

    @Override
    public int intervalMillis() {
        return 0;
    }

    @Override
    public int warningMillis() {
        return 0;
    }

    @Override
    public String warningMessage() {
        return "";
    }

    @Override
    public void attack(BossPatternTarget target) {
    }
}
