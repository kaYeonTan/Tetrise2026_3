package kr.ac.jbnu.se.tetris.network.coop;

/** 온라인 협동 전체 상태 (호스트 → 게스트 동기화·저장). */
public class CoopSnapshot {

	public String coopId;
	public int stage;
	public int bossHp;
	public int bossMax;
	public int bossTimer;
	public long gameTime;
	public boolean stageClear;
	public long blizzardEnd;
	public int blueprintIndex = -1;

	public CoopPlayerSnapshot p1 = new CoopPlayerSnapshot();
	public CoopPlayerSnapshot p2 = new CoopPlayerSnapshot();
}
