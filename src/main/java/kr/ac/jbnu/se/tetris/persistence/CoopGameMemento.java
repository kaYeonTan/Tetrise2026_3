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
	public long bossTimer;
	public long gameTime;
	public boolean stageClear;
	public long blizzardEnd;
	public int blueprintIndex = -1;

	public SessionMemento player0;
	public SessionMemento player1;

	/** 온라인 이어하기 */
	public boolean onlineResume;
	public String lastRoomId;
	public long savedAtMs;
	/** GamePanel 레거시 2P (엔진 미사용) 스냅샷 JSON */
	public String panelSnapshotJson;
}
