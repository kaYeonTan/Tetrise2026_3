package kr.ac.jbnu.se.tetris.boss.brain;

import java.util.List;

import kr.ac.jbnu.se.tetris.Tetrominoes;

/**
 * Chooses the attack whose best possible player landing still has the highest
 * board penalty.
 */
public final class MaxMinBrain {

    private static final int TOP_OUT_SCORE = Integer.MAX_VALUE / 4;

    private final PlacementSearcher placements = new PlacementSearcher();
    private final BoardEvaluator evaluator = new BoardEvaluator();
    private final OptionGenerator options = new OptionGenerator();

    public int chooseGarbageHole(Grid board, Tetrominoes responsePiece) {
        int bestHole = 0;
        int bestScore = Integer.MIN_VALUE;
        for (int hole : options.garbageHoles()) {
            int score = playerBestScore(board.raiseGarbage(hole), responsePiece);
            if (score > bestScore) {
                bestScore = score;
                bestHole = hole;
            }
        }
        return bestHole;
    }

    public Tetrominoes chooseForcedPiece(Grid board) {
        Tetrominoes worstPiece = Tetrominoes.LineShape;
        int worstScore = Integer.MIN_VALUE;
        for (Tetrominoes piece : options.forcedPieces()) {
            int score = playerBestScore(board, piece);
            if (score > worstScore) {
                worstScore = score;
                worstPiece = piece;
            }
        }
        return worstPiece;
    }

    private int playerBestScore(Grid board, Tetrominoes piece) {
        if (board == null) {
            return TOP_OUT_SCORE;
        }
        List<Grid> responses = placements.search(board, piece);
        if (responses.isEmpty()) {
            return TOP_OUT_SCORE;
        }
        int bestScore = Integer.MAX_VALUE;
        for (Grid response : responses) {
            bestScore = Math.min(bestScore, evaluator.evaluate(response));
        }
        return bestScore;
    }
}
