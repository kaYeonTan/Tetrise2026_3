package kr.ac.jbnu.se.tetris.network.coop;



import java.util.function.Consumer;



import javax.swing.SwingUtilities;



import com.google.gson.Gson;

import com.google.gson.GsonBuilder;



import kr.ac.jbnu.se.tetris.GamePanel;

import kr.ac.jbnu.se.tetris.core.GameSession;
import kr.ac.jbnu.se.tetris.core.PlayerAction;

import kr.ac.jbnu.se.tetris.integration.CoopEngineBridge;

import kr.ac.jbnu.se.tetris.network.lobby.LobbyClient;

import kr.ac.jbnu.se.tetris.network.lobby.LobbyMessage;

import kr.ac.jbnu.se.tetris.network.lobby.LobbyMessageType;

import kr.ac.jbnu.se.tetris.persistence.CoopGameMemento;

import kr.ac.jbnu.se.tetris.persistence.MementoMapper;

import kr.ac.jbnu.se.tetris.persistence.SaveService;

import kr.ac.jbnu.se.tetris.persistence.SessionMemento;



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

			panel.isTwoPlayer = true;

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

				if ("BLUEPRINT".equals(msg.action)) {
					String mask = msg.message;
					SwingUtilities.invokeLater(() -> panel.applyGuestBlueprintMask(mask));
				} else {
					SwingUtilities.invokeLater(() -> panel.applyRemoteCoopAction(msg.playerIndex, msg.action));
				}

				break;

			case COOP_SNAPSHOT_PUSH:

				SwingUtilities.invokeLater(() -> applySnapshotJson(msg.snapshotJson));

				break;

			case GUEST_BOARD_PUSH:

				String boardJson = msg.snapshotJson;
				String boardMsg = msg.message;
				SwingUtilities.invokeLater(() -> applyGuestBoard(boardJson, boardMsg));

				break;

			case PEER_DISCONNECTED:

				SwingUtilities.invokeLater(() -> panel.onCoopPeerDisconnected(msg.message));

				break;

			case COOP_SAVE_PUSH:

				SwingUtilities.invokeLater(() -> panel.onHostCoopSavePush(msg.saveSlot, msg.snapshotJson));

				break;

			case COOP_SAVE_OPEN_PUSH:

				SwingUtilities.invokeLater(() -> panel.onHostCoopSaveSessionOpen());

				break;

			case COOP_SAVE_CANCEL_PUSH:

				SwingUtilities.invokeLater(() -> panel.onHostCoopSaveSessionCancelled());

				break;

			default:

				break;

		}

	}



	public void sendBlueprintMask(long mask) {
		if (!isActive() || host) {
			return;
		}
		try {
			LobbyMessage req = LobbyMessage.of(LobbyMessageType.PLAYER_ACTION);
			req.playerIndex = localPlayerIndex;
			req.action = "BLUEPRINT";
			req.message = Long.toHexString(mask);
			client.sendPlayerAction(req);
		} catch (Exception ignored) {
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

	/** 참가자: 내 보드 배열이랑 떨어지는 블록을 방장에게 보낸다. */
	public void maybeSendMyBoard() {
		if (!isActive() || host || panel.screen != 5) {
			return;
		}
		sendMyBoard(false);
	}

	public void sendGameOver() {
		if (!isActive()) {
			return;
		}
		try {
			if (host) {
				if (panel.coopEngineBridge == null) {
					return;
				}
				CoopGameMemento mem = panel.coopEngineBridge.createMemento(coopId);
				if (mem == null) {
					return;
				}
				mem.gameOver = true;
				LobbyMessage req = LobbyMessage.of(LobbyMessageType.COOP_SNAPSHOT);
				req.coopId = coopId;
				req.snapshotJson = GSON.toJson(mem);
				client.sendCoopSnapshot(req);
			} else {
				sendMyBoard(true);
			}
		} catch (Exception ignored) {
		}
	}

	private void sendMyBoard(boolean gameOver) {
		if (panel.coopEngineBridge == null) {
			return;
		}
		GameSession mine = panel.coopEngineBridge.session(localPlayerIndex);
		if (mine == null) {
			return;
		}
		try {
			SessionMemento board = MementoMapper.fromSession(mine);
			LobbyMessage req = LobbyMessage.of(LobbyMessageType.GUEST_BOARD);
			req.playerIndex = localPlayerIndex;
			req.snapshotJson = GSON.toJson(board);
			req.message = gameOver ? "GAMEOVER" : "";
			client.sendCoopSnapshot(req);
		} catch (Exception ignored) {
		}
	}

	private void applyGuestBoard(String json, String message) {
		if (!host || panel.coopEngineBridge == null) {
			return;
		}
		if (json != null && !json.isEmpty()) {
			SessionMemento board = GSON.fromJson(json, SessionMemento.class);
			panel.coopEngineBridge.showGuestBoard(board);
		}
		if ("GAMEOVER".equals(message)) {
			panel.notifyEngineGameOver();
		}
		panel.repaint();
	}



	public void notifyGuestSaveUiOpen() {
		if (!isActive() || !host) {
			return;
		}
		try {
			client.sendCoopSnapshot(LobbyMessage.of(LobbyMessageType.COOP_SAVE_OPEN));
		} catch (Exception ignored) {
		}
	}

	public void notifyGuestSaveUiCancelled() {
		if (!isActive() || !host) {
			return;
		}
		try {
			client.sendCoopSnapshot(LobbyMessage.of(LobbyMessageType.COOP_SAVE_CANCEL));
		} catch (Exception ignored) {
		}
	}

	public void pushLoadedState(CoopGameMemento mem) {
		if (!isActive() || !host || mem == null) {
			return;
		}
		try {
			LobbyMessage req = LobbyMessage.of(LobbyMessageType.COOP_SNAPSHOT);
			req.coopId = coopId != null ? coopId : mem.coopId;
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


