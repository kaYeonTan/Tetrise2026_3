package kr.ac.jbnu.se.tetris.boss;

public interface BlueprintTarget {

    boolean isBlueprintCellOccupied(int row, int column);

    void completeBlueprintPattern();
}
