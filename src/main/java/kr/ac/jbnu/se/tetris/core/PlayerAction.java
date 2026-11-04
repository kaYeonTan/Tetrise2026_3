package kr.ac.jbnu.se.tetris.core;

/** 협동 2인 — 네트워크·로컬 공통 입력 액션. */
public enum PlayerAction {
	MOVE_LEFT,
	MOVE_RIGHT,
	SOFT_DROP,
	HARD_DROP,
	ROTATE_CW,
	ROTATE_CCW,
	HOLD,
	/** 온라인 협동: 양쪽 일시정지 동기화 */
	PAUSE,
	RESUME,
	/** 온라인 협동: 스테이지 클리어 후 다음 스테이지 진행 동의 */
	STAGE_READY,
	/** 참가자 줄 삭제 보고 (점수·보스 데미지는 방장만) */
	LINES_1,
	LINES_2,
	LINES_3,
	LINES_4
}
