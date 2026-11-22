package kr.ac.jbnu.se.tetris.network.lobby;

/** {@link kr.ac.jbnu.se.tetris.LobbyFrame} 대기실 패널에 쓸 표시 상태. */
public record LobbyMyRoomUiState(
		String myRoomText,
		String waitingLabelText,
		boolean startEnabled,
		String startButtonText,
		String rememberedJoinCode,
		String rememberedRoomId) {

	public static final String START_BUTTON = "게임 시작 (방장)";
	public static final String GUEST_NICK = "Guest";

	public static LobbyMyRoomUiState empty() {
		return new LobbyMyRoomUiState(
				"아직 방에 없어요.\n새 방을 만들면\n여기에 대기실 정보가 표시돼요.",
				"로비",
				false,
				START_BUTTON,
				null,
				null);
	}

	public static LobbyMyRoomUiState fromServerResponse(
			LobbyMessage resp,
			String myNick,
			String prevJoinCode,
			String prevRoomId,
			boolean clientConnected) {
		if (resp == null || resp.players == null) {
			return empty();
		}
		boolean host = "HOST".equals(resp.role);
		String hostNick = resp.players.isEmpty() ? myNick : resp.players.get(0);
		Remembered remembered = remember(resp, prevJoinCode, prevRoomId);
		String joinCode = resolveJoinCode(resp, host, remembered);
		boolean isPrivate = isPrivate(resp, host, remembered);
		boolean full = resp.players.size() >= 2;
		String waiting = "대기 중 · " + resp.roomName + " · " + resp.players.size() + "/2";
		String startText = host ? START_BUTTON : "방장이 시작할 때까지 대기";
		String text = roomText(resp, myNick, host, hostNick, isPrivate, joinCode);
		return new LobbyMyRoomUiState(text, waiting, host && full && clientConnected, startText,
				remembered.code, remembered.roomId);
	}

	private static Remembered remember(LobbyMessage resp, String prevJoinCode, String prevRoomId) {
		if (resp.joinCode != null && !resp.joinCode.isEmpty()) {
			return new Remembered(resp.joinCode, resp.roomId);
		}
		return new Remembered(prevJoinCode, prevRoomId);
	}

	private static String resolveJoinCode(LobbyMessage resp, boolean host, Remembered remembered) {
		String joinCode = resp.joinCode;
		boolean missing = joinCode == null || joinCode.isEmpty();
		if (host && missing && resp.roomId != null && resp.roomId.equals(remembered.roomId)) {
			return remembered.code;
		}
		return joinCode;
	}

	private static boolean isPrivate(LobbyMessage resp, boolean host, Remembered remembered) {
		if (RoomVisibility.PRIVATE.name().equals(resp.visibility)) {
			return true;
		}
		return host && remembered.code != null && resp.roomId != null && resp.roomId.equals(remembered.roomId);
	}

	private static String roomText(LobbyMessage resp, String myNick, boolean host, String hostNick,
			boolean isPrivate, String joinCode) {
		StringBuilder sb = new StringBuilder();
		sb.append("방: ").append(resp.roomName).append('\n');
		sb.append(isPrivate ? "종류: 비밀방\n" : "종류: 공개방\n");
		sb.append("───\n");
		appendPlayers(sb, myNick, host, hostNick);
		appendPrivateCode(sb, host, isPrivate, joinCode);
		sb.append("───\n");
		sb.append(resp.players.size()).append(" / 2명");
		sb.append(resp.players.size() < 2 ? " · 상대 대기 중" : " · 준비 완료");
		return sb.toString();
	}

	private static void appendPlayers(StringBuilder sb, String myNick, boolean host, String hostNick) {
		if (host) {
			appendNick(sb, myNick, "방장");
			return;
		}
		sb.append(hostNick).append(" (방장)\n");
		appendNick(sb, myNick, "참가");
	}

	private static void appendNick(StringBuilder sb, String nick, String roleLabel) {
		sb.append(nick);
		if (GUEST_NICK.equalsIgnoreCase(nick)) {
			sb.append(" (게스트)");
		}
		sb.append(" (").append(roleLabel).append(")\n");
	}

	private static void appendPrivateCode(StringBuilder sb, boolean host, boolean isPrivate, String joinCode) {
		if (!host || !isPrivate) {
			return;
		}
		sb.append("───\n");
		if (joinCode != null && !joinCode.isEmpty()) {
			sb.append("입장 코드 (비밀번호):\n").append(joinCode).append('\n');
			sb.append("(친구에게 알려 주세요)\n");
			return;
		}
		sb.append("입장 코드를 불러오지 못했어요.\n방 나가기 후 다시 만들어 주세요.\n");
	}

	private record Remembered(String code, String roomId) {
	}
}
