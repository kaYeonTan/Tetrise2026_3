package kr.ac.jbnu.se.tetris.network.lobby;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RoomRegistryTest {

	RoomRegistry registry;

	@BeforeEach
	void setUp() {
		registry = new RoomRegistry();
	}

	@Test
	void maxFiveRooms() {
		for (int i = 0; i < LobbyConfig.MAX_ROOMS; i++) {
			assertEquals(RoomRegistry.CreateResult.OK,
					registry.createRoom("room" + i, RoomVisibility.PUBLIC, "host" + i).result);
		}
		assertEquals(RoomRegistry.CreateResult.ROOM_CAP,
				registry.createRoom("oneMore", RoomVisibility.PUBLIC, "hostX").result);
	}

	@Test
	void privateRoomRequiresJoinCode() {
		var out = registry.createRoom("secret", RoomVisibility.PRIVATE, "h1");
		assertNotNull(out.room.joinCode);
		assertEquals(RoomRegistry.JoinResult.WRONG_CODE, registry.join(out.room.roomId, "BAD", "g1"));
		assertEquals(RoomRegistry.JoinResult.OK, registry.join(out.room.roomId, out.room.joinCode, "g1"));
		assertEquals(2, out.room.memberClientIds.size());
		assertEquals(RoomRegistry.JoinResult.FULL, registry.join(out.room.roomId, out.room.joinCode, "g2"));
	}

	@Test
	void leaveRemovesEmptyRoom() {
		var out = registry.createRoom("tmp", RoomVisibility.PUBLIC, "only");
		String id = out.room.roomId;
		assertTrue(registry.find(id).isPresent());
		registry.leave("only");
		assertTrue(registry.find(id).isEmpty());
	}
}
