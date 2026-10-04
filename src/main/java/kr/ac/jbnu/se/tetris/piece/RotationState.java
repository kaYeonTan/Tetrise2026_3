package kr.ac.jbnu.se.tetris.piece;

// SRS 회전 상태 0, R, 2, L
public enum RotationState {
	SPAWN,
	R,
	TWO,
	L;

	public RotationState cw() {
		if (this == SPAWN)
			return R;
		if (this == R)
			return TWO;
		if (this == TWO)
			return L;
		return SPAWN;
	}

	public RotationState ccw() {
		if (this == SPAWN)
			return L;
		if (this == L)
			return TWO;
		if (this == TWO)
			return R;
		return SPAWN;
	}
}
