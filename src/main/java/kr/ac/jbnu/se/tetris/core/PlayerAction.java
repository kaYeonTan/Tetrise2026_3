package kr.ac.jbnu.se.tetris.core;

/** 협동 2인 — 네트워크·로컬 공통 입력 액션. */
public enum PlayerAction {
	MOVE_LEFT,
	MOVE_RIGHT,
	SOFT_DROP,
	HARD_DROP,
	ROTATE_CW,
	ROTATE_CCW,
	HOLD
}
