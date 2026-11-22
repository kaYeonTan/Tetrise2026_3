package kr.ac.jbnu.se.tetris.boss;

import kr.ac.jbnu.se.tetris.Tetrominoes;

public interface AccelerationTarget extends BossMessageTarget {

    default Tetrominoes selectForcedPiece() {
        return Tetrominoes.LineShape;
    }

    default void replaceCurrentPiece(Tetrominoes type) {
    }

    void startAcceleration(int durationMillis, int gravityMultiplier, int nextPieceMultiplier);
}
