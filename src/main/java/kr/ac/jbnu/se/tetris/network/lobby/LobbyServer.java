package kr.ac.jbnu.se.tetris.network.lobby;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LobbyServer implements AutoCloseable {

	private static final Logger LOG = Logger.getLogger(LobbyServer.class.getName());
	static final String CODE_INVALID = "INVALID";
	static final String CODE_NOT_HOST = "NOT_HOST";
	static final String CODE_NOT_IN_GAME = "NOT_IN_GAME";
	static final String CODE_NOT_HELLO = "NOT_HELLO";
	static final String CODE_ALREADY_IN_ROOM = "ALREADY_IN_ROOM";
	static final String ROLE_GUEST = "GUEST";
	static final String MSG_NOT_IN_GAME = "진행 중인 게임이 없어요.";
	static final String MSG_NEED_HELLO = "먼저 HELLO로 접속해 주세요.";

	private final RoomRegistry registry = new RoomRegistry();
	private final Map<String, String> clientNicks = new ConcurrentHashMap<>();
	private final Map<String, ClientLink> clientLinks = new ConcurrentHashMap<>();
	private final Map<String, InGameRoom> ingameByRoom = new ConcurrentHashMap<>();
	private ServerSocket serverSocket;
	private ExecutorService pool;
	private volatile boolean running;

	public void start(int port) throws IOException {
		if (running) {
			return;
		}
		serverSocket = new ServerSocket(port);
		pool = Executors.newCachedThreadPool(r -> {
			Thread t = new Thread(r, "lobby-worker");
			t.setDaemon(true);
			return t;
		});
		running = true;
		pool.execute(this::acceptLoop);
		LOG.info("[LobbyServer] listening on port " + port + " (max rooms " + LobbyConfig.MAX_ROOMS + ")");
	}

	private void acceptLoop() {
		while (running) {
			try {
				Socket socket = serverSocket.accept();
				pool.execute(() -> handleClient(socket));
			} catch (IOException e) {
				if (running) {
					LOG.log(Level.WARNING, "accept failed", e);
				}
			}
		}
	}

	private void handleClient(Socket socket) {
		String clientId = UUID.randomUUID().toString();
		ClientLink link = null;
		try (Socket s = socket;
				BufferedReader in = new BufferedReader(
						new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
				BufferedWriter out = new BufferedWriter(
						new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8))) {
			link = new ClientLink(clientId, out);
			clientLinks.put(clientId, link);
			String line;
			while ((line = in.readLine()) != null) {
				LobbyMessage req = LobbyCodec.decode(line);
				LobbyMessage resp = dispatch(clientId, req);
				if (resp != null) {
					link.write(resp);
				}
			}
		} catch (IOException e) {
			LOG.log(Level.FINE, "client disconnected", e);
		} finally {
			onClientDisconnect(clientId);
			if (link != null) {
				clientLinks.remove(clientId, link);
			}
		}
	}

	private void onClientDisconnect(String clientId) {
		registry.roomOfClient(clientId).ifPresent(room -> dissolveRoom(room, clientId, "상대 연결이 끊어졌어요."));
		clientNicks.remove(clientId);
	}

	private void dissolveRoom(LobbyRoom room, String initiatorClientId, String peerMessage) {
		if (room == null) {
			return;
		}
		ingameByRoom.remove(room.roomId);
		notifyPeer(room, initiatorClientId, LobbyMessage.of(LobbyMessageType.PEER_DISCONNECTED), m -> {
			m.message = peerMessage;
		});
		registry.removeRoom(room.roomId);
	}

	static final class ClientLink {
		final String clientId;
		final BufferedWriter out;

		ClientLink(String clientId, BufferedWriter out) {
			this.clientId = clientId;
			this.out = out;
		}

		synchronized void write(LobbyMessage msg) throws IOException {
			out.write(LobbyCodec.encode(msg));
			out.newLine();
			out.flush();
		}
	}

	void pushTo(String clientId, LobbyMessage msg) {
		ClientLink link = clientLinks.get(clientId);
		if (link == null) {
			return;
		}
		try {
			link.write(msg);
		} catch (IOException ex) {
			LOG.log(Level.FINE, "push failed", ex);
		}
	}

	void notifyPeer(LobbyRoom room, String fromClientId, LobbyMessage template,
			java.util.function.Consumer<LobbyMessage> fill) {
		for (String member : room.memberClientIds) {
			if (member.equals(fromClientId)) {
				continue;
			}
			LobbyMessage push = LobbyMessage.of(LobbyMessageType.valueOf(template.type));
			fill.accept(push);
			pushTo(member, push);
		}
	}

	private LobbyMessage dispatch(String clientId, LobbyMessage req) {
		if (req == null || req.type == null) {
			return error(CODE_INVALID, "잘못된 요청이에요.");
		}
		LobbyMessageType type;
		try {
			type = LobbyMessageType.valueOf(req.type);
		} catch (IllegalArgumentException e) {
			return error(CODE_INVALID, "알 수 없는 요청이에요.");
		}
		switch (type) {
			case HELLO:
				return handleHello(clientId, req);
			case LIST_ROOMS:
				return handleListRooms();
			case CREATE_ROOM:
				return handleCreateRoom(clientId, req);
			case JOIN_ROOM:
				return handleJoinRoom(clientId, req);
			case LEAVE_ROOM:
				return handleLeaveRoom(clientId);
			case MY_ROOM:
				return handleMyRoom(clientId);
			case START_GAME:
				return handleStartGame(clientId, req);
			case RESUME_COOP:
				return handleResumeCoop(clientId, req);
			case PLAYER_ACTION:
				return handlePlayerAction(clientId, req);
			case COOP_SNAPSHOT:
				return handleCoopSnapshot(clientId, req);
			case GUEST_BOARD:
				return handleGuestBoard(clientId, req);
			case COOP_SAVE:
				return handleCoopSave(clientId, req);
			case COOP_SAVE_OPEN:
				return handleCoopSaveOpen(clientId);
			case COOP_SAVE_CANCEL:
				return handleCoopSaveCancel(clientId);
			default:
				return error(CODE_INVALID, "로비에서 지원하지 않는 요청이에요.");
		}
	}

	private LobbyMessage handleHello(String clientId, LobbyMessage req) {
		String nick = req.nick == null ? LobbyMyRoomUiState.GUEST_NICK : req.nick.trim();
		if (nick.isEmpty()) {
			nick = LobbyMyRoomUiState.GUEST_NICK;
		}
		if (nick.length() > 20) {
			nick = nick.substring(0, 20);
		}
		clientNicks.put(clientId, nick);
		LobbyMessage ok = LobbyMessage.of(LobbyMessageType.HELLO_OK);
		ok.clientId = clientId;
		ok.nick = nick;
		return ok;
	}

	private LobbyMessage handleStartGame(String clientId, LobbyMessage req) {
		return beginInGame(clientId, req, false);
	}

	private LobbyMessage handleResumeCoop(String clientId, LobbyMessage req) {
		if (req.coopId == null || req.coopId.isBlank() || req.snapshotJson == null || req.snapshotJson.isBlank()) {
			return error(CODE_INVALID, "저장 데이터가 없어요.");
		}
		return beginInGame(clientId, req, true);
	}

	private LobbyMessage beginInGame(String clientId, LobbyMessage req, boolean resume) {
		LobbyRoom room = registry.roomOfClient(clientId).orElse(null);
		if (room == null) {
			return error("NOT_IN_ROOM", "방에 들어가 있지 않아요.");
		}
		if (!clientId.equals(room.hostClientId)) {
			return error(CODE_NOT_HOST, "방장만 게임을 시작할 수 있어요.");
		}
		if (room.memberClientIds.size() < LobbyConfig.MAX_PLAYERS_PER_ROOM) {
			return error("NOT_READY", "2명이 모여야 시작할 수 있어요.");
		}
		InGameRoom game;
		if (resume) {
			long seed = req.seed > 0 ? req.seed : System.currentTimeMillis();
			game = new InGameRoom(room.roomId, room.hostClientId, req.coopId.trim(), seed);
			game.pendingSnapshotJson = req.snapshotJson;
			ingameByRoom.put(room.roomId, game);
		} else {
			game = ingameByRoom.computeIfAbsent(room.roomId, id -> new InGameRoom(room.roomId, room.hostClientId));
		}
		game.started = true;

		List<String> roster = playerNicksOrdered(room);
		for (String member : room.memberClientIds) {
			if (member.equals(clientId)) {
				continue;
			}
			LobbyMessage push = LobbyMessage.of(LobbyMessageType.GAME_STARTED);
			push.roomId = room.roomId;
			push.coopId = game.coopId;
			push.seed = game.seed;
			fillOnlinePlayerAssignment(push, room, member);
			push.players = roster;
			push.resumeGame = resume;
			if (resume) {
				push.snapshotJson = game.pendingSnapshotJson;
			}
			pushTo(member, push);
		}

		LobbyMessage ok = LobbyMessage.of(LobbyMessageType.START_GAME_OK);
		ok.roomId = room.roomId;
		ok.coopId = game.coopId;
		ok.seed = game.seed;
		fillOnlinePlayerAssignment(ok, room, clientId);
		ok.players = roster;
		ok.resumeGame = resume;
		if (resume) {
			ok.snapshotJson = game.pendingSnapshotJson;
		}
		return ok;
	}

	private LobbyMessage handlePlayerAction(String clientId, LobbyMessage req) {
		LobbyRoom room = registry.roomOfClient(clientId).orElse(null);
		if (room == null || !ingameByRoom.containsKey(room.roomId)) {
			return error(CODE_NOT_IN_GAME, MSG_NOT_IN_GAME);
		}
		int expected = clientId.equals(room.hostClientId) ? 0 : 1;
		if (req.playerIndex != expected) {
			return error("INVALID_PLAYER", "플레이어 번호가 맞지 않아요.");
		}
		notifyPeer(room, clientId, LobbyMessage.of(LobbyMessageType.PLAYER_ACTION), push -> {
			push.playerIndex = req.playerIndex;
			push.action = req.action;
			push.message = req.message;
		});
		return null;
	}

	private LobbyMessage handleCoopSnapshot(String clientId, LobbyMessage req) {
		LobbyRoom room = registry.roomOfClient(clientId).orElse(null);
		if (room == null || !clientId.equals(room.hostClientId)) {
			return error(CODE_NOT_HOST, "방장만 스냅샷을 보낼 수 있어요.");
		}
		notifyPeer(room, clientId, LobbyMessage.of(LobbyMessageType.COOP_SNAPSHOT_PUSH), push -> {
			push.coopId = req.coopId;
			push.snapshotJson = req.snapshotJson;
		});
		return null;
	}

	// 참가자 보드를 방장에게 그대로 넘긴다
	private LobbyMessage handleGuestBoard(String clientId, LobbyMessage req) {
		LobbyRoom room = registry.roomOfClient(clientId).orElse(null);
		if (room == null || !ingameByRoom.containsKey(room.roomId)) {
			return error(CODE_NOT_IN_GAME, MSG_NOT_IN_GAME);
		}
		if (clientId.equals(room.hostClientId)) {
			return error("NOT_GUEST", "참가자만 보드를 보낼 수 있어요.");
		}
		notifyPeer(room, clientId, LobbyMessage.of(LobbyMessageType.GUEST_BOARD_PUSH), push -> {
			push.playerIndex = req.playerIndex;
			push.snapshotJson = req.snapshotJson;
			push.message = req.message;
		});
		return null;
	}

	private LobbyMessage handleCoopSaveOpen(String clientId) {
		LobbyRoom room = registry.roomOfClient(clientId).orElse(null);
		if (room == null || !clientId.equals(room.hostClientId)) {
			return error(CODE_NOT_HOST, "방장만 저장을 시작할 수 있어요.");
		}
		if (!ingameByRoom.containsKey(room.roomId)) {
			return error(CODE_NOT_IN_GAME, MSG_NOT_IN_GAME);
		}
		notifyPeer(room, clientId, LobbyMessage.of(LobbyMessageType.COOP_SAVE_OPEN_PUSH), push -> {
		});
		return null;
	}

	private LobbyMessage handleCoopSaveCancel(String clientId) {
		LobbyRoom room = registry.roomOfClient(clientId).orElse(null);
		if (room == null || !clientId.equals(room.hostClientId)) {
			return error(CODE_NOT_HOST, "방장만 저장을 취소할 수 있어요.");
		}
		if (!ingameByRoom.containsKey(room.roomId)) {
			return error(CODE_NOT_IN_GAME, MSG_NOT_IN_GAME);
		}
		notifyPeer(room, clientId, LobbyMessage.of(LobbyMessageType.COOP_SAVE_CANCEL_PUSH), push -> {
		});
		return null;
	}

	private LobbyMessage handleCoopSave(String clientId, LobbyMessage req) {
		LobbyRoom room = registry.roomOfClient(clientId).orElse(null);
		if (room == null || !clientId.equals(room.hostClientId)) {
			return error(CODE_NOT_HOST, "방장만 저장을 요청할 수 있어요.");
		}
		if (!ingameByRoom.containsKey(room.roomId)) {
			return error(CODE_NOT_IN_GAME, MSG_NOT_IN_GAME);
		}
		notifyPeer(room, clientId, LobbyMessage.of(LobbyMessageType.COOP_SAVE_PUSH), push -> {
			push.coopId = req.coopId;
			push.saveSlot = req.saveSlot;
			push.snapshotJson = req.snapshotJson;
		});
		LobbyMessage ok = LobbyMessage.of(LobbyMessageType.COOP_SAVE_OK);
		ok.coopId = req.coopId;
		ok.saveSlot = req.saveSlot;
		return ok;
	}

	private LobbyMessage handleListRooms() {
		List<RoomSnapshot> list = new ArrayList<>();
		for (LobbyRoom room : registry.allRooms()) {
			String hostNick = clientNicks.getOrDefault(room.hostClientId, "?");
			list.add(room.toSnapshot(hostNick));
		}
		LobbyMessage resp = LobbyMessage.of(LobbyMessageType.ROOM_LIST);
		resp.rooms = list;
		return resp;
	}

	private LobbyMessage handleCreateRoom(String clientId, LobbyMessage req) {
		if (!clientNicks.containsKey(clientId)) {
			return error(CODE_NOT_HELLO, MSG_NEED_HELLO);
		}
		if (registry.roomOfClient(clientId).isPresent()) {
			return error(CODE_ALREADY_IN_ROOM, "이미 방에 있어요. 나간 뒤 다시 만들어 주세요.");
		}
		RoomVisibility vis;
		try {
			vis = RoomVisibility.valueOf(req.visibility == null ? "PUBLIC" : req.visibility);
		} catch (IllegalArgumentException e) {
			return error("INVALID_VISIBILITY", "공개/비밀 방을 선택해 주세요.");
		}
		RoomRegistry.CreateOutcome outcome = registry.createRoom(req.name, vis, clientId);
		switch (outcome.result) {
			case INVALID_NAME:
				return error("INVALID_NAME", "방 이름은 1~32자로 입력해 주세요.");
			case ROOM_CAP:
				return error("ROOM_CAP", "서버 방 개수 한도(" + LobbyConfig.MAX_ROOMS + "개)에 도달했어요.");
			case OK:
				LobbyRoom room = outcome.room;
				LobbyMessage created = LobbyMessage.of(LobbyMessageType.ROOM_CREATED);
				created.roomId = room.roomId;
				created.roomName = room.name;
				created.visibility = room.visibility.name();
				created.role = "HOST";
				if (room.visibility == RoomVisibility.PRIVATE) {
					created.joinCode = room.joinCode;
				}
				created.players = playerNicks(room);
				return created;
			default:
				return error("UNKNOWN", "방을 만들지 못했어요.");
		}
	}

	private LobbyMessage handleJoinRoom(String clientId, LobbyMessage req) {
		if (!clientNicks.containsKey(clientId)) {
			return error(CODE_NOT_HELLO, MSG_NEED_HELLO);
		}
		if (registry.roomOfClient(clientId).isPresent()) {
			return error(CODE_ALREADY_IN_ROOM, "이미 다른 방에 있어요.");
		}
		RoomRegistry.JoinResult result = registry.join(req.roomId, req.joinCode, clientId);
		switch (result) {
			case NOT_FOUND:
				return error("NOT_FOUND", "방을 찾을 수 없어요.");
			case FULL:
				return error("FULL", "방이 가득 찼어요 (2/2).");
			case WRONG_CODE:
				return error("WRONG_CODE", "입장 코드가 맞지 않아요.");
			case ALREADY_IN_ROOM:
				return error(CODE_ALREADY_IN_ROOM, "이미 이 방에 있어요.");
			case OK:
				LobbyRoom room = registry.find(req.roomId).orElse(null);
				LobbyMessage joined = LobbyMessage.of(LobbyMessageType.JOINED);
				joined.roomId = req.roomId;
				joined.roomName = room != null ? room.name : "";
				if (room != null) {
					joined.visibility = room.visibility.name();
				}
				joined.role = room != null && clientId.equals(room.hostClientId) ? "HOST" : ROLE_GUEST;
				joined.players = room != null ? playerNicks(room) : new ArrayList<>();
				return joined;
			default:
				return error("UNKNOWN", "입장하지 못했어요.");
		}
	}

	private LobbyMessage handleLeaveRoom(String clientId) {
		registry.roomOfClient(clientId)
				.ifPresent(room -> dissolveRoom(room, clientId, "방이 종료되었어요."));
		return LobbyMessage.of(LobbyMessageType.LEFT);
	}

	private LobbyMessage handleMyRoom(String clientId) {
		if (!clientNicks.containsKey(clientId)) {
			return error(CODE_NOT_HELLO, MSG_NEED_HELLO);
		}
		LobbyRoom room = registry.roomOfClient(clientId).orElse(null);
		if (room == null) {
			LobbyMessage none = LobbyMessage.of(LobbyMessageType.LEFT);
			none.message = "NOT_IN_ROOM";
			return none;
		}
		LobbyMessage joined = LobbyMessage.of(LobbyMessageType.JOINED);
		joined.roomId = room.roomId;
		joined.roomName = room.name;
		joined.visibility = room.visibility.name();
		joined.role = clientId.equals(room.hostClientId) ? "HOST" : ROLE_GUEST;
		joined.players = playerNicks(room);
		if (room.visibility == RoomVisibility.PRIVATE && clientId.equals(room.hostClientId)) {
			joined.joinCode = room.joinCode;
		}
		return joined;
	}

	/** P1(왼쪽)=방장, P2(오른쪽)=입장자 순서로 닉네임 목록. */
	private List<String> playerNicksOrdered(LobbyRoom room) {
		List<String> nicks = new ArrayList<>();
		nicks.add(clientNicks.getOrDefault(room.hostClientId, "?"));
		for (String id : room.memberClientIds) {
			if (!id.equals(room.hostClientId)) {
				nicks.add(clientNicks.getOrDefault(id, "?"));
			}
		}
		return nicks;
	}

	private List<String> playerNicks(LobbyRoom room) {
		return playerNicksOrdered(room);
	}

	/** 방장=playerIndex 0 (P1), 입장자=1 (P2). */
	private void fillOnlinePlayerAssignment(LobbyMessage msg, LobbyRoom room, String recipientClientId) {
		boolean isHost = recipientClientId.equals(room.hostClientId);
		msg.playerIndex = isHost ? 0 : 1;
		msg.role = isHost ? "HOST" : ROLE_GUEST;
	}

	private static LobbyMessage error(String code, String message) {
		LobbyMessage err = LobbyMessage.of(LobbyMessageType.ERROR);
		err.code = code;
		err.message = message;
		return err;
	}

	public RoomRegistry getRegistry() {
		return registry;
	}

	@Override
	public void close() throws IOException {
		running = false;
		if (serverSocket != null) {
			serverSocket.close();
		}
		if (pool != null) {
			pool.shutdownNow();
		}
	}
}
