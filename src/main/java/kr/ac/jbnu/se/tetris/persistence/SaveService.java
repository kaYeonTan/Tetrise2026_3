package kr.ac.jbnu.se.tetris.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/** F-06 — 협동 슬롯 1~3 (게스트는 `_local` 계정). */
public final class SaveService {

	public static final int MIN_SLOT = 1;
	public static final int MAX_SLOT = 3;

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private SaveService() {
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
		if (slot < MIN_SLOT || slot > MAX_SLOT) {
			throw new IOException("슬롯은 1~3만 사용할 수 있어요.");
		}
		if (memento == null || memento.coopId == null || memento.coopId.isEmpty()) {
			throw new IOException("coopId가 없어요.");
		}
		memento.savedAtMs = System.currentTimeMillis();
		String userId = storageUserId(loginUser);
		Path dir = Paths.get("data", "saves", userId);
		Files.createDirectories(dir);
		Path target = slotPath(userId, slot);
		Path tmp = dir.resolve("slot" + slot + ".json.tmp");
		Files.writeString(tmp, GSON.toJson(memento), StandardCharsets.UTF_8);
		Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);

		Path coopBody = Paths.get("data", "coop", memento.coopId + ".json");
		Files.createDirectories(coopBody.getParent());
		Path coopTmp = Paths.get("data", "coop", memento.coopId + ".json.tmp");
		Files.writeString(coopTmp, GSON.toJson(memento), StandardCharsets.UTF_8);
		Files.move(coopTmp, coopBody, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
	}

	public static CoopGameMemento loadCoop(String loginUser, int slot) throws LoadException {
		if (slot < MIN_SLOT || slot > MAX_SLOT) {
			throw new LoadException("슬롯 번호가 올바르지 않아요.");
		}
		Path path = slotPath(storageUserId(loginUser), slot);
		if (!Files.isRegularFile(path)) {
			throw new LoadException("비어 있는 슬롯이에요.");
		}
		try {
			CoopGameMemento m = GSON.fromJson(Files.readString(path, StandardCharsets.UTF_8), CoopGameMemento.class);
			if (m == null || m.version != CoopGameMemento.VERSION) {
				throw new LoadException("저장 형식이 맞지 않아요.");
			}
			return m;
		} catch (JsonSyntaxException | IOException e) {
			throw new LoadException("저장 파일을 읽지 못했어요.", e);
		}
	}

	public static List<SaveSlotMeta> listCoopSlots(String loginUser) {
		String userId = storageUserId(loginUser);
		List<SaveSlotMeta> out = new ArrayList<>();
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
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
				String when = m.savedAtMs > 0 ? fmt.format(new Date(m.savedAtMs)) : "?";
				String kind = m.onlineResume ? "온라인 협동" : "협동";
				out.add(new SaveSlotMeta(s, true,
						"슬롯 " + s + " · " + kind + " · ST" + m.stage + " · " + when, m.savedAtMs));
			} catch (Exception e) {
				out.add(new SaveSlotMeta(s, false, "읽기 실패", 0));
			}
		}
		return out;
	}

	public static void deleteSlot(String loginUser, int slot) throws IOException {
		Path path = slotPath(storageUserId(loginUser), slot);
		Files.deleteIfExists(path);
	}
}
