package kr.ac.jbnu.se.tetris.core;

import kr.ac.jbnu.se.tetris.GamePanel;

/** 스테이지·보스 등 두 플레이어가 공유하는 협동 진행 상태. */
public final class CoopRunState {

	public int stage = 1;
	public int bossHp;
	public int bossMax;
	public int bossTimer;
	public long gameTime;
	public boolean stageClear;
	public long blizzardEnd;
	public int hitEffect;
	public boolean bossWarning;
	public String bossWarningText = "";

	public void readFrom(GamePanel p) {
		stage = p.stage;
		bossHp = p.bossHp;
		bossMax = p.bossMax;
		bossTimer = p.bossTimer;
		gameTime = p.gameTime;
		stageClear = p.stageClear;
		blizzardEnd = p.blizzardEnd;
		hitEffect = p.getHitEffect();
		bossWarning = p.bossWarning;
		bossWarningText = p.bossWarningText == null ? "" : p.bossWarningText;
	}

	public void writeTo(GamePanel p) {
		p.stage = stage;
		p.bossHp = bossHp;
		p.bossMax = bossMax;
		p.bossTimer = bossTimer;
		p.gameTime = gameTime;
		p.stageClear = stageClear;
		p.blizzardEnd = blizzardEnd;
		p.setHitEffect(hitEffect);
		p.bossWarning = bossWarning;
		p.bossWarningText = bossWarningText;
	}
}
