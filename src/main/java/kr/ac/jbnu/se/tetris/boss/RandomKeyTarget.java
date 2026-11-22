package kr.ac.jbnu.se.tetris.boss;

public interface RandomKeyTarget extends BossMessageTarget {

    int[] primaryOriginalKeys();

    int[] secondaryOriginalKeys();

    boolean isTwoPlayerMode();

    void applyTemporaryKeyMappings(int[] primaryMapping, int[] secondaryMapping, int durationMillis);
}
