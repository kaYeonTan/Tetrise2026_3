package kr.ac.jbnu.se.tetris;

import java.util.Random;

import kr.ac.jbnu.se.tetris.piece.RotationState;

public class Shape {

	private Tetrominoes pieceShape;
	private int coords[][];
	private int blockCount;
	private RotationState rotationState = RotationState.SPAWN;

	public Shape() {
		coords = new int[5][2];
		setShape(Tetrominoes.NoShape);
	}

	public static boolean isPentomino(Tetrominoes shape) {
		return shape == Tetrominoes.P5_P || shape == Tetrominoes.P5_U || shape == Tetrominoes.P5_V
				|| shape == Tetrominoes.P5_X || shape == Tetrominoes.P5_F;
	}

	public int blockCount() {
		return blockCount;
	}

	public void setShape(Tetrominoes shape) {
		pieceShape = shape;
		rotationState = RotationState.SPAWN;

		if (shape == Tetrominoes.NoShape) {
			blockCount = 0;
			return;
		}

		int[][] table = spawnCoords(shape);
		blockCount = isPentomino(shape) ? 5 : 4;
		for (int i = 0; i < blockCount; i++) {
			coords[i][0] = table[i][0];
			coords[i][1] = table[i][1];
		}
	}

	static int[][] spawnCoords(Tetrominoes shape) {
		switch (shape) {
		case ZShape:
			return new int[][] { { 0, -1 }, { 0, 0 }, { -1, 0 }, { -1, 1 } };
		case SShape:
			return new int[][] { { 0, -1 }, { 0, 0 }, { 1, 0 }, { 1, 1 } };
		case LineShape:
			return new int[][] { { 0, -1 }, { 0, 0 }, { 0, 1 }, { 0, 2 } };
		case TShape:
			return new int[][] { { -1, 0 }, { 0, 0 }, { 1, 0 }, { 0, 1 } };
		case SquareShape:
			return new int[][] { { 0, 0 }, { 1, 0 }, { 0, 1 }, { 1, 1 } };
		case LShape:
			return new int[][] { { -1, -1 }, { 0, -1 }, { 0, 0 }, { 0, 1 } };
		case MirroredLShape:
			return new int[][] { { 1, -1 }, { 0, -1 }, { 0, 0 }, { 0, 1 } };
		case P5_P:
			return new int[][] { { 0, 0 }, { 1, 0 }, { 0, 1 }, { 1, 1 }, { 1, 2 } };
		case P5_U:
			return new int[][] { { 0, 0 }, { 2, 0 }, { 0, 1 }, { 1, 1 }, { 2, 1 } };
		case P5_V:
			return new int[][] { { 0, 0 }, { 0, 1 }, { 0, 2 }, { 1, 2 }, { 2, 2 } };
		case P5_X:
			return new int[][] { { 1, 0 }, { 0, 1 }, { 1, 1 }, { 2, 1 }, { 1, 2 } };
		case P5_F:
			return new int[][] { { 1, 0 }, { 2, 0 }, { 0, 1 }, { 1, 1 }, { 1, 2 } };
		default:
			return new int[][] { { 0, 0 }, { 0, 0 }, { 0, 0 }, { 0, 0 } };
		}
	}

	public RotationState getRotationState() {
		return rotationState;
	}

	public void setRotationState(RotationState rotationState) {
		this.rotationState = rotationState;
	}

	private void setX(int index, int x) {
		coords[index][0] = x;
	}

	private void setY(int index, int y) {
		coords[index][1] = y;
	}

	public int x(int index) {
		return coords[index][0];
	}

	public int y(int index) {
		return coords[index][1];
	}

	public Tetrominoes getShape() {
		return pieceShape;
	}

	public void setRandomShape() {
		Random r = new Random();
		int x = Math.abs(r.nextInt()) % 7 + 1;
		Tetrominoes[] values = Tetrominoes.values();
		setShape(values[x]);
	}

	public int minX() {
		int m = coords[0][0];
		for (int i = 0; i < blockCount; i++)
			m = Math.min(m, coords[i][0]);
		return m;
	}

	public int minY() {
		int m = coords[0][1];
		for (int i = 0; i < blockCount; i++)
			m = Math.min(m, coords[i][1]);
		return m;
	}

	public Shape rotateLeft() {
		if (pieceShape == Tetrominoes.SquareShape)
			return this;

		Shape result = new Shape();
		result.pieceShape = pieceShape;
		result.blockCount = blockCount;
		result.rotationState = rotationState;

		for (int i = 0; i < blockCount; ++i) {
			result.setX(i, y(i));
			result.setY(i, -x(i));
		}
		return result;
	}

	public Shape rotateRight() {
		if (pieceShape == Tetrominoes.SquareShape)
			return this;

		Shape result = new Shape();
		result.pieceShape = pieceShape;
		result.blockCount = blockCount;
		result.rotationState = rotationState;

		for (int i = 0; i < blockCount; ++i) {
			result.setX(i, -y(i));
			result.setY(i, x(i));
		}
		return result;
	}

	public int[][] copyCoords() {
		int[][] c = new int[blockCount][2];
		for (int i = 0; i < blockCount; i++) {
			c[i][0] = coords[i][0];
			c[i][1] = coords[i][1];
		}
		return c;
	}
}
