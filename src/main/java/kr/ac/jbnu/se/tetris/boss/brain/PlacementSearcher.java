package kr.ac.jbnu.se.tetris.boss.brain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import kr.ac.jbnu.se.tetris.Shape;
import kr.ac.jbnu.se.tetris.Tetrominoes;

/** Finds every reachable hard-drop landing for a tetromino. */
public final class PlacementSearcher {

    public List<Grid> search(Grid board, Tetrominoes piece) {
        if (board == null || piece == null || piece == Tetrominoes.NoShape) {
            return List.of();
        }
        List<Grid> results = new ArrayList<>();
        for (int[][] rotation : rotations(piece)) {
            int minX = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;
            for (int[] block : rotation) {
                minX = Math.min(minX, block[0]);
                maxX = Math.max(maxX, block[0]);
                maxY = Math.max(maxY, block[1]);
            }
            for (int x = -minX; x < Grid.WIDTH - maxX; x++) {
                int startY = Grid.HEIGHT - 1 - maxY;
                if (!board.canPlace(rotation, x, startY)) {
                    continue;
                }
                int y = startY;
                while (y > 0 && board.canPlace(rotation, x, y - 1)) {
                    y--;
                }
                results.add(board.placeAndClear(rotation, x, y));
            }
        }
        return results;
    }

    private List<int[][]> rotations(Tetrominoes type) {
        List<int[][]> rotations = new ArrayList<>();
        Shape shape = new Shape();
        shape.setShape(type);
        for (int turn = 0; turn < 4; turn++) {
            int[][] coordinates = normalized(shape.copyCoords());
            if (rotations.stream().noneMatch(existing -> Arrays.deepEquals(existing, coordinates))) {
                rotations.add(coordinates);
            }
            shape = shape.rotateRight();
        }
        return rotations;
    }

    private int[][] normalized(int[][] coordinates) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        for (int[] coordinate : coordinates) {
            minX = Math.min(minX, coordinate[0]);
            minY = Math.min(minY, coordinate[1]);
        }
        int[][] normalized = new int[coordinates.length][2];
        for (int i = 0; i < coordinates.length; i++) {
            normalized[i][0] = coordinates[i][0] - minX;
            normalized[i][1] = coordinates[i][1] - minY;
        }
        Arrays.sort(normalized, Comparator.comparingInt((int[] coordinate) -> coordinate[0])
                .thenComparingInt(coordinate -> coordinate[1]));
        return normalized;
    }
}
