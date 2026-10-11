package kr.ac.jbnu.se.tetris.boss.brain;

/** Scores a board from the boss's perspective: larger values are worse for the player. */
public final class BoardEvaluator {

    public int evaluate(Grid grid) {
        int[] heights = new int[Grid.WIDTH];
        int holes = 0;
        for (int x = 0; x < Grid.WIDTH; x++) {
            boolean blockSeen = false;
            for (int y = Grid.HEIGHT - 1; y >= 0; y--) {
                if (grid.occupied(x, y)) {
                    blockSeen = true;
                    heights[x] = Math.max(heights[x], y + 1);
                } else if (blockSeen) {
                    holes++;
                }
            }
        }
        int aggregateHeight = 0;
        int bumpiness = 0;
        for (int x = 0; x < Grid.WIDTH; x++) {
            aggregateHeight += heights[x];
            if (x > 0) {
                bumpiness += Math.abs(heights[x] - heights[x - 1]);
            }
        }
        return aggregateHeight * 10 + holes * 20 + bumpiness * 5;
    }
}
