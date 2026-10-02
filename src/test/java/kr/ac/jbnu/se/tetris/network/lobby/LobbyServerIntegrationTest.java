package kr.ac.jbnu.se.tetris.network.lobby;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LobbyServerIntegrationTest {

	LobbyServer server;
	int port;

	@BeforeEach
	void startServer() throws Exception {
		port = 18765;
		server = new LobbyServer();
		server.start(port);
		Thread.sleep(80);
	}

	@AfterEach
	void stopServer() throws Exception {
		server.close();
	}

	@Test
	void createListJoin() throws Exception {
		try (LobbyClient host = new LobbyClient()) {
			host.connect("127.0.0.1", port, "HostNick");
			LobbyMessage created = host.createRoom("Desert Run", RoomVisibility.PRIVATE);
			assertEquals(LobbyMessageType.ROOM_CREATED.name(), created.type);
			assertNotNull(created.joinCode);

			try (LobbyClient guest = new LobbyClient()) {
				guest.connect("127.0.0.1", port, "GuestNick");
				var rooms = guest.listRooms();
				assertEquals(1, rooms.size());
				assertTrue(rooms.get(0).joinable);

				LobbyMessage joined = guest.joinRoom(created.roomId, created.joinCode);
				assertEquals(LobbyMessageType.JOINED.name(), joined.type);
				assertEquals(2, joined.players.size());
			}
		}
	}

	@Test
	void startGameWhenFull() throws Exception {
		try (LobbyClient host = new LobbyClient()) {
			host.connect("127.0.0.1", port, "Host");
			LobbyMessage created = host.createRoom("Coop", RoomVisibility.PUBLIC);
			try (LobbyClient guest = new LobbyClient()) {
				guest.connect("127.0.0.1", port, "Guest");
				guest.joinRoom(created.roomId, null);
				LobbyMessage started = host.startGame();
				assertEquals(LobbyMessageType.START_GAME_OK.name(), started.type);
				assertNotNull(started.coopId);
				assertEquals(0, started.playerIndex);
			}
		}
	}

	@Test
	void roomCapOnServer() throws Exception {
		for (int i = 0; i < LobbyConfig.MAX_ROOMS; i++) {
			try (LobbyClient c = new LobbyClient()) {
				c.connect("127.0.0.1", port, "u" + i);
				LobbyMessage m = c.createRoom("r" + i, RoomVisibility.PUBLIC);
				assertEquals(LobbyMessageType.ROOM_CREATED.name(), m.type);
			}
		}
		try (LobbyClient extra = new LobbyClient()) {
			extra.connect("127.0.0.1", port, "overflow");
			LobbyMessage m = extra.createRoom("no", RoomVisibility.PUBLIC);
			assertEquals(LobbyMessageType.ERROR.name(), m.type);
			assertEquals("ROOM_CAP", m.code);
		}
	}
}
