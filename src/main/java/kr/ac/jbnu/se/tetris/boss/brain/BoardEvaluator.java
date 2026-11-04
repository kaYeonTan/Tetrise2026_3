package kr.ac.jbnu.se.tetris.boss.brain;

/** Scores a board from the boss's perspective: larger values are worse for the player. */
public final class BoardEvaluator {

    private static final int HEIGHT_WEIGHT = 10;
    private static final int HOLE_WEIGHT = 20;
    private static final int BUMPINESS_WEIGHT = 5;

    public int evaluate(Grid grid) {
        int[] heights = new int[Grid.WIDTH];
        int holes = measureHeightsAndHoles(grid, heights);
        return sum(heights) * HEIGHT_WEIGHT + holes * HOLE_WEIGHT + bumpiness(heights) * BUMPINESS_WEIGHT;
    }

    /** 열마다 가장 높은 블록의 높이를 heights에 채우고, 블록 아래 빈 칸(구멍)의 수를 돌려준다. */
    private static int measureHeightsAndHoles(Grid grid, int[] heights) {
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
        return holes;
    }

    private static int sum(int[] values) {
        int total = 0;
        for (int v : values) {
            total += v;
        }
        return total;
    }

    private static int bumpiness(int[] heights) {
        int total = 0;
        for (int x = 1; x < heights.length; x++) {
            total += Math.abs(heights[x] - heights[x - 1]);
        }
        return total;
    }
}
