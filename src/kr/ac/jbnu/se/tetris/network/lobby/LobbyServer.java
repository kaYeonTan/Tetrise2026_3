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

public class LobbyServer implements AutoCloseable {

	private final RoomRegistry registry = new RoomRegistry();
	private final Map<String, String> clientNicks = new ConcurrentHashMap<>();
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
		System.out.println("[LobbyServer] listening on port " + port + " (max rooms " + LobbyConfig.MAX_ROOMS + ")");
	}

	private void acceptLoop() {
		while (running) {
			try {
				Socket socket = serverSocket.accept();
				pool.execute(() -> handleClient(socket));
			} catch (IOException e) {
				if (running) {
					e.printStackTrace();
				}
			}
		}
	}

	private void handleClient(Socket socket) {
		String clientId = UUID.randomUUID().toString();
		try (Socket s = socket;
				BufferedReader in = new BufferedReader(
						new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
				BufferedWriter out = new BufferedWriter(
						new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8))) {
			String line;
			while ((line = in.readLine()) != null) {
				LobbyMessage req = LobbyCodec.decode(line);
				LobbyMessage resp = dispatch(clientId, req);
				out.write(LobbyCodec.encode(resp));
				out.newLine();
				out.flush();
			}
		} catch (IOException e) {
			// disconnect
		} finally {
			registry.leave(clientId);
			clientNicks.remove(clientId);
		}
	}

	private LobbyMessage dispatch(String clientId, LobbyMessage req) {
		if (req == null || req.type == null) {
			return error("INVALID", "잘못된 요청이에요.");
		}
		LobbyMessageType type;
		try {
			type = LobbyMessageType.valueOf(req.type);
		} catch (IllegalArgumentException e) {
			return error("INVALID", "알 수 없는 요청이에요.");
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
			default:
				return error("INVALID", "로비에서 지원하지 않는 요청이에요.");
		}
	}

	private LobbyMessage handleHello(String clientId, LobbyMessage req) {
		String nick = req.nick == null ? "Guest" : req.nick.trim();
		if (nick.isEmpty()) {
			nick = "Guest";
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
			return error("NOT_HELLO", "먼저 HELLO로 접속해 주세요.");
		}
		if (registry.roomOfClient(clientId).isPresent()) {
			return error("ALREADY_IN_ROOM", "이미 방에 있어요. 나간 뒤 다시 만들어 주세요.");
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
			return error("NOT_HELLO", "먼저 HELLO로 접속해 주세요.");
		}
		if (registry.roomOfClient(clientId).isPresent()) {
			return error("ALREADY_IN_ROOM", "이미 다른 방에 있어요.");
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
				return error("ALREADY_IN_ROOM", "이미 이 방에 있어요.");
			case OK:
				LobbyRoom room = registry.find(req.roomId).orElse(null);
				LobbyMessage joined = LobbyMessage.of(LobbyMessageType.JOINED);
				joined.roomId = req.roomId;
				joined.roomName = room != null ? room.name : "";
				if (room != null) {
					joined.visibility = room.visibility.name();
				}
				joined.role = room != null && clientId.equals(room.hostClientId) ? "HOST" : "GUEST";
				joined.players = room != null ? playerNicks(room) : new ArrayList<>();
				return joined;
			default:
				return error("UNKNOWN", "입장하지 못했어요.");
		}
	}

	private LobbyMessage handleLeaveRoom(String clientId) {
		registry.leave(clientId);
		return LobbyMessage.of(LobbyMessageType.LEFT);
	}

	private LobbyMessage handleMyRoom(String clientId) {
		if (!clientNicks.containsKey(clientId)) {
			return error("NOT_HELLO", "먼저 HELLO로 접속해 주세요.");
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
		joined.role = clientId.equals(room.hostClientId) ? "HOST" : "GUEST";
		joined.players = playerNicks(room);
		if (room.visibility == RoomVisibility.PRIVATE && clientId.equals(room.hostClientId)) {
			joined.joinCode = room.joinCode;
		}
		return joined;
	}

	private List<String> playerNicks(LobbyRoom room) {
		List<String> nicks = new ArrayList<>();
		for (String id : room.memberClientIds) {
			nicks.add(clientNicks.getOrDefault(id, "?"));
		}
		return nicks;
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
