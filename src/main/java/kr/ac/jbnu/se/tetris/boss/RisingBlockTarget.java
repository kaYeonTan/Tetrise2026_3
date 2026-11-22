package kr.ac.jbnu.se.tetris.boss;

public interface RisingBlockTarget extends BossMessageTarget {

    int selectGarbageHole();

    void raiseBlocks(int garbageHole);
}
