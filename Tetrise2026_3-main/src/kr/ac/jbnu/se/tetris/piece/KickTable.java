package kr.ac.jbnu.se.tetris.piece;

// 가이드라인 SRS wall kick (dx, dy) dy는 아래가 +
public class KickTable {

	static final int[][] JLSTZ_0_R = { { 0, 0 }, { -1, 0 }, { -1, 1 }, { 0, -2 }, { -1, -2 } };
	static final int[][] JLSTZ_R_0 = { { 0, 0 }, { 1, 0 }, { 1, -1 }, { 0, 2 }, { 1, 2 } };
	static final int[][] JLSTZ_R_2 = { { 0, 0 }, { 1, 0 }, { 1, 1 }, { 0, -2 }, { 1, -2 } };
	static final int[][] JLSTZ_2_R = { { 0, 0 }, { -1, 0 }, { -1, -1 }, { 0, 2 }, { -1, 2 } };
	static final int[][] JLSTZ_2_L = { { 0, 0 }, { 1, 0 }, { 1, -1 }, { 0, 2 }, { 1, 2 } };
	static final int[][] JLSTZ_L_2 = { { 0, 0 }, { -1, 0 }, { -1, 1 }, { 0, -2 }, { -1, -2 } };
	static final int[][] JLSTZ_L_0 = { { 0, 0 }, { -1, 0 }, { -1, -1 }, { 0, 2 }, { -1, 2 } };
	static final int[][] JLSTZ_0_L = { { 0, 0 }, { 1, 0 }, { 1, 1 }, { 0, -2 }, { 1, -2 } };

	static final int[][] I_0_R = { { 0, 0 }, { -2, 0 }, { 1, 0 }, { -2, -1 }, { 1, 2 } };
	static final int[][] I_R_0 = { { 0, 0 }, { 2, 0 }, { -1, 0 }, { 2, 1 }, { -1, -2 } };
	static final int[][] I_R_2 = { { 0, 0 }, { -1, 0 }, { 2, 0 }, { -1, 2 }, { 2, -1 } };
	static final int[][] I_2_R = { { 0, 0 }, { 1, 0 }, { -2, 0 }, { 1, -2 }, { -2, 1 } };
	static final int[][] I_2_L = { { 0, 0 }, { 2, 0 }, { -1, 0 }, { 2, 1 }, { -1, -2 } };
	static final int[][] I_L_2 = { { 0, 0 }, { -2, 0 }, { 1, 0 }, { -2, -1 }, { 1, 2 } };
	static final int[][] I_L_0 = { { 0, 0 }, { 1, 0 }, { -2, 0 }, { 1, -2 }, { -2, 1 } };
	static final int[][] I_0_L = { { 0, 0 }, { -1, 0 }, { 2, 0 }, { -1, 2 }, { 2, -1 } };

	static final int[][] O_ANY = { { 0, 0 } };

	public static int[][] offsets(KickGroup group, RotationState from, RotationState to) {
		if (group == KickGroup.O)
			return O_ANY;

		if (group == KickGroup.I) {
			if (from == RotationState.SPAWN && to == RotationState.R)
				return I_0_R;
			if (from == RotationState.R && to == RotationState.SPAWN)
				return I_R_0;
			if (from == RotationState.R && to == RotationState.TWO)
				return I_R_2;
			if (from == RotationState.TWO && to == RotationState.R)
				return I_2_R;
			if (from == RotationState.TWO && to == RotationState.L)
				return I_2_L;
			if (from == RotationState.L && to == RotationState.TWO)
				return I_L_2;
			if (from == RotationState.L && to == RotationState.SPAWN)
				return I_L_0;
			if (from == RotationState.SPAWN && to == RotationState.L)
				return I_0_L;
		}

		if (from == RotationState.SPAWN && to == RotationState.R)
			return JLSTZ_0_R;
		if (from == RotationState.R && to == RotationState.SPAWN)
			return JLSTZ_R_0;
		if (from == RotationState.R && to == RotationState.TWO)
			return JLSTZ_R_2;
		if (from == RotationState.TWO && to == RotationState.R)
			return JLSTZ_2_R;
		if (from == RotationState.TWO && to == RotationState.L)
			return JLSTZ_2_L;
		if (from == RotationState.L && to == RotationState.TWO)
			return JLSTZ_L_2;
		if (from == RotationState.L && to == RotationState.SPAWN)
			return JLSTZ_L_0;
		if (from == RotationState.SPAWN && to == RotationState.L)
			return JLSTZ_0_L;

		return O_ANY;
	}
}
