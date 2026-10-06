package kr.ac.jbnu.se.tetris.network.coop;



import java.util.function.Consumer;



import javax.swing.SwingUtilities;



import com.google.gson.Gson;

import com.google.gson.GsonBuilder;



import kr.ac.jbnu.se.tetris.GamePanel;

import kr.ac.jbnu.se.tetris.core.PlayerAction;

import kr.ac.jbnu.se.tetris.integration.CoopEngineBridge;

import kr.ac.jbnu.se.tetris.network.lobby.LobbyClient;

import kr.ac.jbnu.se.tetris.network.lobby.LobbyMessage;

import kr.ac.jbnu.se.tetris.network.lobby.LobbyMessageType;

import kr.ac.jbnu.se.tetris.persistence.CoopGameMemento;

import kr.ac.jbnu.se.tetris.persistence.SaveService;



/** GamePanel ↔ LobbyClient in-game 동기화. */

public final class CoopNetworkBridge {



	private static final Gson GSON = new GsonBuilder().create();



	private final GamePanel panel;

	private LobbyClient client;

	private String coopId;

	private int localPlayerIndex;

	private boolean host;

	private int snapshotTick;



	public CoopNetworkBridge(GamePanel panel) {

		this.panel = panel;

	}



	public void attach(LobbyClient client) {

		this.client = client;

		client.setPushHandler(this::onPush);

	}



	public void detach() {

		if (client != null) {

			client.setPushHandler(null);

		}

		client = null;

	}



	public boolean isActive() {

		return panel.isOnlineCoop && client != null && client.isConnected();

	}



	public boolean isHost() {

		return host;

	}



	public String getCoopId() {

		return coopId;

	}



	public int getLocalPlayerIndex() {

		return localPlayerIndex;

	}



	public void beginFromStartMessage(LobbyMessage started) {

		coopId = started.coopId;

		localPlayerIndex = resolveLocalPlayerIndex(started);

		host = localPlayerIndex == 0;

		panel.applyOnlineCoopRoster(started.players, localPlayerIndex);

		panel.lastOnlineRoomId = started.roomId;



		if (started.resumeGame && started.snapshotJson != null && !started.snapshotJson.isBlank()) {

			CoopGameMemento mem = GSON.fromJson(started.snapshotJson, CoopGameMemento.class);

			panel.isOnlineCoop = true;

			panel.localPlayerIndex = localPlayerIndex;

			panel.onlineCoopId = coopId;

			panel.closeOnlineLobbyUi();

			if (host) {

				CoopEngineBridge.applyLoadedMemento(panel, mem);

			} else {

				if (panel.coopEngineBridge == null) {

					panel.coopEngineBridge = new CoopEngineBridge(panel);

				}

				panel.coopEngineBridge.syncGuestFromMemento(mem);

				panel.screen = 5;

				panel.msg = "온라인 협동 · 참가 (P2) · 저장 이어하기";

				panel.msgTimer = 150;

			}

			return;

		}



		panel.beginOnlineCoop(localPlayerIndex, coopId, started.seed);

	}

	private static int resolveLocalPlayerIndex(LobbyMessage started) {
		if ("HOST".equals(started.role)) {
			return 0;
		}
		if ("GUEST".equals(started.role)) {
			return 1;
		}
		if (started.playerIndex == 0 || started.playerIndex == 1) {
			return started.playerIndex;
		}
		return 0;
	}



	void onPush(LobbyMessage msg) {

		if (msg == null || msg.type == null) {

			return;

		}

		LobbyMessageType type;

		try {

			type = LobbyMessageType.valueOf(msg.type);

		} catch (IllegalArgumentException e) {

			return;

		}

		switch (type) {

			case GAME_STARTED:

				SwingUtilities.invokeLater(() -> beginFromStartMessage(msg));

				break;

			case PLAYER_ACTION:

				SwingUtilities.invokeLater(() -> panel.applyRemoteCoopAction(msg.playerIndex, msg.action));

				break;

			case COOP_SNAPSHOT_PUSH:

				SwingUtilities.invokeLater(() -> applySnapshotJson(msg.snapshotJson));

				break;

			case PEER_DISCONNECTED:

				SwingUtilities.invokeLater(() -> panel.onCoopPeerDisconnected(msg.message));

				break;

			default:

				break;

		}

	}



	public void sendLocalAction(PlayerAction action) {

		if (!isActive() || action == null) {

			return;

		}

		try {

			LobbyMessage req = LobbyMessage.of(LobbyMessageType.PLAYER_ACTION);

			req.playerIndex = localPlayerIndex;

			req.action = action.name();

			client.sendPlayerAction(req);

		} catch (Exception ignored) {

		}

	}



	public void maybeSendSnapshot() {

		if (!isActive() || !host) {

			return;

		}

		// 일시정지 중에도 참가자 화면·오버레이 동기화용 스냅샷 전송
		if (panel.screen != 5 && panel.screen != 6) {

			return;

		}

		snapshotTick++;

		try {

			if (panel.useCoopEngineHost() && panel.coopEngineBridge != null) {

				panel.coopEngineBridge.syncBoardsToPanel();

			}

			CoopGameMemento mem = panel.useCoopEngineHost() && panel.coopEngineBridge != null

					? panel.coopEngineBridge.createMemento(coopId)

					: panel.buildCoopMementoFromPanel();

			LobbyMessage req = LobbyMessage.of(LobbyMessageType.COOP_SNAPSHOT);

			req.coopId = coopId;

			req.snapshotJson = GSON.toJson(mem);

			client.sendCoopSnapshot(req);

		} catch (Exception ignored) {

		}

	}



	public void notifySaved(int slot, CoopGameMemento mem) throws Exception {

		if (!isActive() || !host) {

			return;

		}

		LobbyMessage req = LobbyMessage.of(LobbyMessageType.COOP_SAVE);

		req.coopId = coopId;

		req.snapshotJson = GSON.toJson(mem);

		req.saveSlot = slot;

		client.requestCoopSave(req);

	}



	private void applySnapshotJson(String json) {

		if (json == null || json.isEmpty() || host) {

			return;

		}

		CoopGameMemento mem = GSON.fromJson(json, CoopGameMemento.class);

		if (mem == null) {

			return;

		}

		if (panel.coopEngineBridge == null) {

			panel.coopEngineBridge = new CoopEngineBridge(panel);

		}

		panel.coopEngineBridge.syncGuestFromMemento(mem);
		panel.repaint();

	}



	public static Consumer<LobbyMessage> noop() {

		return m -> {

		};

	}

}


