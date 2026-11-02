package kr.ac.jbnu.se.tetris.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class SaveService {

	public static final int MIN_SLOT = 1;
	public static final int MAX_SLOT = 3;

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static CoopGameRepository repository = new JsonCoopGameRepository();

	private SaveService() {
	}

	static void setRepositoryForTests(CoopGameRepository repo) {
		repository = repo;
	}

	static void resetRepositoryForTests() {
		repository = new JsonCoopGameRepository();
	}

	public static String storageUserId(String loginUser) {
		if (loginUser == null || loginUser.isBlank()) {
			return "_local";
		}
		return loginUser.trim();
	}

	static Path slotPath(String userId, int slot) {
		return Paths.get("data", "saves", userId, "slot" + slot + ".json");
	}

	public static void saveCoop(String loginUser, int slot, CoopGameMemento memento) throws IOException {
		repository.saveCoop(loginUser, slot, memento);
	}

	public static CoopGameMemento loadCoop(String loginUser, int slot) throws LoadException {
		CoopGameMemento m = readCoopSlot(loginUser, slot);
		assertCanLoadCoop(m, loginUser);
		return m;
	}

	static CoopGameMemento readCoopSlot(String loginUser, int slot) throws LoadException {
		return repository.readCoopSlot(loginUser, slot);
	}

	public static void assertCanLoadCoop(CoopGameMemento m, String loginUser) throws LoadException {
		if (m == null || !m.onlineResume) {
			return;
		}
		if (loginUser == null || loginUser.isBlank()) {
			throw new LoadException("온라인 협동 저장은 로그인 후 불러올 수 있어요.");
		}
		if (m.hostAccountId == null || m.hostAccountId.isBlank()
				|| m.guestAccountId == null || m.guestAccountId.isBlank()) {
			throw new LoadException("예전 형식 저장이라 온라인 이어하기를 할 수 없어요.");
		}
		String me = loginUser.trim();
		if (!me.equals(m.hostAccountId) && !me.equals(m.guestAccountId)) {
			throw new LoadException("이 저장은 당시 함께 한 두 계정만 불러올 수 있어요.");
		}
	}

	public static boolean allCoopSlotsOccupied(String loginUser) {
		String userId = storageUserId(loginUser);
		for (int s = MIN_SLOT; s <= MAX_SLOT; s++) {
			if (!Files.isRegularFile(slotPath(userId, s))) {
				return false;
			}
		}
		return true;
	}

	public static int coopCombinedScore(CoopGameMemento m) {
		if (m == null) {
			return 0;
		}
		if (m.player0 != null && m.player1 != null) {
			return m.player0.score + m.player1.score;
		}
		return 0;
	}

	public static String coopPartnerNick(CoopGameMemento m, String viewerAccount) {
		if (m == null || viewerAccount == null || viewerAccount.isBlank()) {
			return null;
		}
		String me = viewerAccount.trim();
		if (m.hostAccountId != null && me.equals(m.hostAccountId)) {
			return m.guestAccountId;
		}
		if (m.guestAccountId != null && me.equals(m.guestAccountId)) {
			return m.hostAccountId;
		}
		if (m.hostAccountId != null && m.guestAccountId != null) {
			return me.equals(m.hostAccountId) ? m.guestAccountId : m.hostAccountId;
		}
		return null;
	}

	public static String formatCoopSlotSummary(CoopGameMemento m, String viewerAccount, int slot) {
		if (m == null) {
			return "슬롯 " + slot + " · 손상됨";
		}
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String when = m.savedAtMs > 0 ? fmt.format(new Date(m.savedAtMs)) : "?";
		String kind = m.onlineResume ? "온라인 협동" : "로컬 협동";
		String partner = coopPartnerNick(m, viewerAccount);
		String with = partner != null && !partner.isBlank() ? " · " + partner + "와 함께" : "";
		int score = coopCombinedScore(m);
		return String.format("슬롯 %d · %s%s · ST%d · 합계 %,d · %s", slot, kind, with, m.stage, score, when);
	}

	public static List<SaveSlotMeta> listCoopSlots(String loginUser) {
		String userId = storageUserId(loginUser);
		List<SaveSlotMeta> out = new ArrayList<>();
		for (int s = MIN_SLOT; s <= MAX_SLOT; s++) {
			Path path = slotPath(userId, s);
			if (!Files.isRegularFile(path)) {
				out.add(new SaveSlotMeta(s, false, "비어 있음", 0));
				continue;
			}
			try {
				CoopGameMemento m = GSON.fromJson(Files.readString(path, StandardCharsets.UTF_8), CoopGameMemento.class);
				if (m == null) {
					out.add(new SaveSlotMeta(s, false, "손상됨", 0));
					continue;
				}
				out.add(new SaveSlotMeta(s, true,
						formatCoopSlotSummary(m, loginUser, s), m.savedAtMs));
			} catch (Exception e) {
				out.add(new SaveSlotMeta(s, false, "읽기 실패", 0));
			}
		}
		return out;
	}

	public static void deleteSlot(String loginUser, int slot) throws IOException {
		repository.deleteSlot(loginUser, slot);
	}

	public static CoopGameMemento copyCoopMemento(CoopGameMemento m) {
		return repository.copyMemento(m);
	}

	public static void assertOnlineRoomMatchesSave(CoopGameMemento m, String roomHostNick, String roomGuestNick)
			throws LoadException {
		if (m == null || !m.onlineResume) {
			return;
		}
		if (roomHostNick == null || roomGuestNick == null || roomHostNick.isBlank() || roomGuestNick.isBlank()) {
			throw new LoadException("온라인 협동은 2명이 함께 있을 때만 불러올 수 있어요.");
		}
		if (m.hostAccountId == null || m.guestAccountId == null) {
			throw new LoadException("예전 형식 저장이라 불러올 수 없어요.");
		}
		Set<String> saved = new HashSet<>();
		saved.add(m.hostAccountId.trim());
		saved.add(m.guestAccountId.trim());
		Set<String> room = new HashSet<>();
		room.add(roomHostNick.trim());
		room.add(roomGuestNick.trim());
		if (!saved.equals(room)) {
			throw new LoadException("지금 방의 두 사람이 저장 당시와 같아야 불러올 수 있어요.");
		}
	}

	public static CoopGameMemento remapForCurrentOnlineHost(CoopGameMemento m, String currentHostNick,
			String currentGuestNick) {
		CoopGameMemento out = copyCoopMemento(m);
		if (out == null || currentHostNick == null || out.hostAccountId == null) {
			return out;
		}
		String curHost = currentHostNick.trim();
		String curGuest = currentGuestNick != null ? currentGuestNick.trim() : "";
		if (curHost.equals(out.hostAccountId.trim())) {
			out.hostAccountId = curHost;
			if (!curGuest.isEmpty()) {
				out.guestAccountId = curGuest;
			}
			return out;
		}
		if (!curHost.equals(out.guestAccountId.trim()) || !curGuest.equals(out.hostAccountId.trim())) {
			return out;
		}
		out.swapEnginePlayerSlots();
		out.hostAccountId = curHost;
		out.guestAccountId = curGuest;
		return out;
	}
}
