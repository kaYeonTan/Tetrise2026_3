package kr.ac.jbnu.se.tetris.boss;

public interface BossPattern {

    int intervalMillis();

    int warningMillis();

    String warningMessage();

    void attack(BossPatternTarget target);
}
