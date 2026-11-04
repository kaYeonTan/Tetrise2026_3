package kr.ac.jbnu.se.tetris;

/** 화면/전략이 GamePanel 전체 대신 필요한 상태만 읽도록 하는 읽기 전용 뷰. */
public interface GameContext {
    boolean isTwoPlayerMode();

    boolean onlineCoop();

    int localPlayerIndex();

    int currentStage();

    Player player(int index);
}
