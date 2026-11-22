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

	private static final int[][][][] JLSTZ = new int[4][4][][];
	private static final int[][][][] I_KICKS = new int[4][4][][];

	static {
		put(JLSTZ, RotationState.SPAWN, RotationState.R, JLSTZ_0_R);
		put(JLSTZ, RotationState.R, RotationState.SPAWN, JLSTZ_R_0);
		put(JLSTZ, RotationState.R, RotationState.TWO, JLSTZ_R_2);
		put(JLSTZ, RotationState.TWO, RotationState.R, JLSTZ_2_R);
		put(JLSTZ, RotationState.TWO, RotationState.L, JLSTZ_2_L);
		put(JLSTZ, RotationState.L, RotationState.TWO, JLSTZ_L_2);
		put(JLSTZ, RotationState.L, RotationState.SPAWN, JLSTZ_L_0);
		put(JLSTZ, RotationState.SPAWN, RotationState.L, JLSTZ_0_L);

		put(I_KICKS, RotationState.SPAWN, RotationState.R, I_0_R);
		put(I_KICKS, RotationState.R, RotationState.SPAWN, I_R_0);
		put(I_KICKS, RotationState.R, RotationState.TWO, I_R_2);
		put(I_KICKS, RotationState.TWO, RotationState.R, I_2_R);
		put(I_KICKS, RotationState.TWO, RotationState.L, I_2_L);
		put(I_KICKS, RotationState.L, RotationState.TWO, I_L_2);
		put(I_KICKS, RotationState.L, RotationState.SPAWN, I_L_0);
		put(I_KICKS, RotationState.SPAWN, RotationState.L, I_0_L);
	}

	private KickTable() {
	}

	public static int[][] offsets(KickGroup group, RotationState from, RotationState to) {
		if (group == KickGroup.O || from == null || to == null)
			return O_ANY;
		int[][] found = table(group)[from.ordinal()][to.ordinal()];
		return found != null ? found : O_ANY;
	}

	private static int[][][][] table(KickGroup group) {
		return group == KickGroup.I ? I_KICKS : JLSTZ;
	}

	private static void put(int[][][][] table, RotationState from, RotationState to, int[][] kicks) {
		table[from.ordinal()][to.ordinal()] = kicks;
	}
}
