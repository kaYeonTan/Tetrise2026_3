package kr.ac.jbnu.se.tetris.network.lobby;

public enum LobbyMessageType {
	HELLO,
	LIST_ROOMS,
	CREATE_ROOM,
	JOIN_ROOM,
	LEAVE_ROOM,
	MY_ROOM,

	HELLO_OK,
	ROOM_LIST,
	ROOM_CREATED,
	JOINED,
	LEFT,
	ROOM_UPDATE,
	ERROR
}
