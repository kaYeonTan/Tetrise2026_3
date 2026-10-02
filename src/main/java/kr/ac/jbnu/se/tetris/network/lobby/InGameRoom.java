package kr.ac.jbnu.se.tetris.network.lobby;

import java.util.UUID;

/** 로비 방 1개에 대응하는 in-game 세션 메타. */
public final class InGameRoom {

	public final String roomId;
	public String coopId;
	public long seed;
	public final String hostClientId;
	public volatile boolean started;
	public String pendingSnapshotJson;

	public InGameRoom(String roomId, String hostClientId) {
		this.roomId = roomId;
		this.hostClientId = hostClientId;
		this.coopId = UUID.randomUUID().toString();
		this.seed = System.currentTimeMillis();
	}

	public InGameRoom(String roomId, String hostClientId, String coopId, long seed) {
		this.roomId = roomId;
		this.hostClientId = hostClientId;
		this.coopId = coopId;
		this.seed = seed;
	}
}
