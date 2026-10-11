package kr.ac.jbnu.se.tetris.boss.brain;

import java.util.List;

import kr.ac.jbnu.se.tetris.Tetrominoes;

/** Generates the legal attack candidates for the adaptive boss stages. */
public final class OptionGenerator {

    private static final List<Tetrominoes> FORCED_PIECES = List.of(
            Tetrominoes.LineShape, Tetrominoes.SquareShape, Tetrominoes.TShape,
            Tetrominoes.SShape, Tetrominoes.ZShape, Tetrominoes.LShape, Tetrominoes.MirroredLShape);

    public List<Integer> garbageHoles() {
        return java.util.stream.IntStream.range(0, Grid.WIDTH).boxed().toList();
    }

    public List<Tetrominoes> forcedPieces() {
        return FORCED_PIECES;
    }
}
