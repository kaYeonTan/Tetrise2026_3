package kr.ac.jbnu.se.tetris.network.lobby;

import java.io.IOException;

import com.google.gson.Gson;

import kr.ac.jbnu.se.tetris.GamePanel;
import kr.ac.jbnu.se.tetris.persistence.CoopGameMemento;
import kr.ac.jbnu.se.tetris.persistence.LoadException;
import kr.ac.jbnu.se.tetris.persistence.SaveService;

/** 저장 이어하기 후 「게임 시작」 요청 준비. UI 메시지는 호출 측에서 표시. */
public final class LobbyCoopResumeFlow {

	public sealed interface StartRequest permits StartRequest.Fresh, StartRequest.Resume {

		record Fresh() implements StartRequest {
		}

		record Resume(String coopId, long seed, String snapshotJson) implements StartRequest {
		}
	}

	public record ValidationError(String userMessage) {
	}

	private LobbyCoopResumeFlow() {
	}

	/**
	 * {@code panel.pendingCoopResume}가 있으면 방·계정 검증 후 리맵하고, 서버에 보낼 시작 요청을 만든다.
	 * 검증 실패 시 {@link ValidationError}, 성공 시 pending 은 비운다.
	 */
	public static Object prepareStart(LobbyClient client, GamePanel panel) throws IOException {
		CoopGameMemento pending = panel.pendingCoopResume;
		if (pending == null) {
			return new StartRequest.Fresh();
		}
		LobbyMessage room = LobbyRoomFlow.fetchMyRoom(client);
		if (room == null || room.players == null || room.players.size() < 2) {
			return new ValidationError("2명이 모여야 저장 이어하기를 할 수 있어요.");
		}
		if (pending.hostAccountId == null || pending.guestAccountId == null) {
			return new ValidationError("예전 형식 저장이라 온라인 이어하기를 할 수 없어요.");
		}
		String hostNick = room.players.get(0);
		String guestNick = room.players.get(1);
		try {
			SaveService.assertOnlineRoomMatchesSave(pending, hostNick, guestNick);
		} catch (LoadException ex) {
			return new ValidationError(ex.getMessage());
		}
		CoopGameMemento remapped = SaveService.remapForCurrentOnlineHost(pending, hostNick, guestNick);
		panel.pendingCoopResume = null;
		Gson gson = new Gson();
		String json = gson.toJson(remapped);
		return new StartRequest.Resume(remapped.coopId, remapped.seed, json);
	}

	public static LobbyMessage executeStart(LobbyClient client, StartRequest request) throws IOException {
		if (request instanceof StartRequest.Fresh) {
			return LobbyRoomFlow.startFreshGame(client);
		}
		StartRequest.Resume r = (StartRequest.Resume) request;
		return LobbyRoomFlow.resumeCoopGame(client, r.coopId(), r.seed(), r.snapshotJson());
	}
}