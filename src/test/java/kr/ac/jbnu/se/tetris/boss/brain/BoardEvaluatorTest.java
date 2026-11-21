package kr.ac.jbnu.se.tetris.boss.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BoardEvaluatorTest {

    private final BoardEvaluator evaluator = new BoardEvaluator();

    private static Grid gridWith(int... xy) {
        boolean[][] cells = new boolean[Grid.HEIGHT][Grid.WIDTH];
        for (int i = 0; i < xy.length; i += 2) {
            cells[xy[i + 1]][xy[i]] = true;
        }
        return Grid.fromOccupied(cells);
    }

    @Test
    void emptyBoardScoresZero() {
        assertEquals(0, evaluator.evaluate(Grid.empty()));
    }

    @Test
    void singleBlockCountsHeightAndBumpiness() {
        // 높이 1(10) + 이웃 열과의 높이 차 1(5)
        assertEquals(15, evaluator.evaluate(gridWith(0, 0)));
    }

    @Test
    void floatingBlockAddsHolePenalty() {
        // 높이 2(20) + 구멍 1(20) + 높이 차 2(10)
        assertEquals(50, evaluator.evaluate(gridWith(0, 1)));
    }
}
