package kr.ac.jbnu.se.tetris.network.lobby;

/** {@link kr.ac.jbnu.se.tetris.LobbyFrame} 대기실 패널에 쓸 표시 상태. */
public record LobbyMyRoomUiState(
		String myRoomText,
		String waitingLabelText,
		boolean startEnabled,
		String startButtonText,
		String rememberedJoinCode,
		String rememberedRoomId) {

	public static LobbyMyRoomUiState empty() {
		return new LobbyMyRoomUiState(
				"아직 방에 없어요.\n새 방을 만들면\n여기에 대기실 정보가 표시돼요.",
				"로비",
				false,
				"게임 시작 (방장)",
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
		String hostNick = resp.players.isEmpty() ? myNick : resp.players.get(0);
		boolean host = "HOST".equals(resp.role);
		String joinCode = resp.joinCode;
		String rememberedJoinCode = prevJoinCode;
		String rememberedRoomId = prevRoomId;
		if (joinCode != null && !joinCode.isEmpty()) {
			rememberedJoinCode = joinCode;
			rememberedRoomId = resp.roomId;
		}
		boolean isPrivate = RoomVisibility.PRIVATE.name().equals(resp.visibility)
				|| (host && rememberedJoinCode != null && resp.roomId != null && resp.roomId.equals(rememberedRoomId));
		if (host && (joinCode == null || joinCode.isEmpty()) && resp.roomId != null && resp.roomId.equals(rememberedRoomId)) {
			joinCode = rememberedJoinCode;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("방: ").append(resp.roomName).append('\n');
		sb.append(isPrivate ? "종류: 비밀방\n" : "종류: 공개방\n");
		sb.append("───\n");
		if (host) {
			sb.append(myNick);
			if ("Guest".equalsIgnoreCase(myNick)) {
				sb.append(" (게스트)");
			}
			sb.append(" (방장)\n");
		} else {
			sb.append(hostNick).append(" (방장)\n");
			sb.append(myNick);
			if ("Guest".equalsIgnoreCase(myNick)) {
				sb.append(" (게스트)");
			}
			sb.append(" (참가)\n");
		}
		if (host && isPrivate) {
			sb.append("───\n");
			if (joinCode != null && !joinCode.isEmpty()) {
				sb.append("입장 코드 (비밀번호):\n").append(joinCode).append('\n');
				sb.append("(친구에게 알려 주세요)\n");
			} else {
				sb.append("입장 코드를 불러오지 못했어요.\n방 나가기 후 다시 만들어 주세요.\n");
			}
		}
		sb.append("───\n");
		sb.append(resp.players.size()).append(" / 2명");
		if (resp.players.size() < 2) {
			sb.append(" · 상대 대기 중");
		} else {
			sb.append(" · 준비 완료");
		}

		boolean isHostRole = "HOST".equals(resp.role);
		boolean full = resp.players.size() >= 2;
		boolean startEnabled = isHostRole && full && clientConnected;
		String startText = isHostRole ? "게임 시작 (방장)" : "방장이 시작할 때까지 대기";
		String waiting = "대기 중 · " + resp.roomName + " · " + resp.players.size() + "/2";

		return new LobbyMyRoomUiState(sb.toString(), waiting, startEnabled, startText, rememberedJoinCode,
				rememberedRoomId);
	}
}