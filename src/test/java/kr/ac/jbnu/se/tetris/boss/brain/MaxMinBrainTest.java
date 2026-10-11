package kr.ac.jbnu.se.tetris.boss.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import kr.ac.jbnu.se.tetris.Tetrominoes;
import org.junit.jupiter.api.Test;

class MaxMinBrainTest {

    @Test
    void placementSearchEnumeratesAllLinePieceLandingsOnEmptyBoard() {
        List<Grid> landings = new PlacementSearcher().search(Grid.empty(), Tetrominoes.LineShape);

        assertEquals(17, landings.size());
    }

    @Test
    void evaluatorPenalizesHolesAndUnevenSurface() {
        boolean[][] smoothCells = new boolean[Grid.HEIGHT][Grid.WIDTH];
        boolean[][] unevenCells = new boolean[Grid.HEIGHT][Grid.WIDTH];
        for (int x = 0; x < 4; x++) {
            smoothCells[0][x] = true;
        }
        unevenCells[0][0] = true;
        unevenCells[2][0] = true;

        BoardEvaluator evaluator = new BoardEvaluator();
        assertTrue(evaluator.evaluate(Grid.fromOccupied(unevenCells))
                > evaluator.evaluate(Grid.fromOccupied(smoothCells)));
    }

    @Test
    void garbageHoleCandidatesCoverEveryColumnAndPreserveTheHole() {
        OptionGenerator options = new OptionGenerator();
        Grid raised = Grid.empty().raiseGarbage(4);

        assertEquals(10, options.garbageHoles().size());
        assertFalse(raised.occupied(4, 0));
        assertTrue(raised.occupied(3, 0));
        assertEquals(7, options.forcedPieces().size());
    }

    @Test
    void brainAlwaysReturnsLegalAttackCandidates() {
        MaxMinBrain brain = new MaxMinBrain();

        int hole = brain.chooseGarbageHole(Grid.empty(), Tetrominoes.TShape);
        Tetrominoes forced = brain.chooseForcedPiece(Grid.empty());

        assertTrue(hole >= 0 && hole < Grid.WIDTH);
        assertTrue(new OptionGenerator().forcedPieces().contains(forced));
    }
}
