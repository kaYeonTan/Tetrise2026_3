package kr.ac.jbnu.se.tetris.persistence;

/** 협동 2인 + 공유 진행 (온라인 재접속 메타 포함). */
public class CoopGameMemento {

	public static final int VERSION = 1;

	public int version = VERSION;
	public String coopId;
	public long seed;
	public int stage;
	public int bossHp;
	public int bossMax;
	public int bossTimer;
	public long gameTime;
	public boolean stageClear;
	/** 한쪽이 게임오버면 상대도 결과 화면으로 */
	public boolean gameOver;
	/** 클리어 연출 후 다음 스테이지 확인 패널 (온라인, 방장 권위) */
	public boolean stageAdvancePrompt;
	public boolean stageReadyPlayer0;
	public boolean stageReadyPlayer1;
	public long blizzardEnd;
	public int blueprintIndex = -1;
	/** 스테이지 4 가속 등 */
	public int gravityMultiplier = 1;
	/** 스테이지 5 조작키 뒤섞기 (gameTime 기준 만료) */
	public long randomKeyEnd;
	public int[] randomKeyForPlayer0;
	public int[] randomKeyForPlayer1;
	/** 스테이지 2: 방장이 올린 쓰레기 줄. 참가자는 seq가 늘면 자기 보드에만 적용 */
	public int garbageSeq;
	public int garbageHole0 = -1;
	public int garbageHole1 = -1;

	public SessionMemento player0;
	public SessionMemento player1;

	/** 온라인 이어하기 */
	public boolean onlineResume;
	public String lastRoomId;
	public long savedAtMs;
	/** GamePanel 레거시 2P (엔진 미사용) 스냅샷 JSON */
	public String panelSnapshotJson;
}
