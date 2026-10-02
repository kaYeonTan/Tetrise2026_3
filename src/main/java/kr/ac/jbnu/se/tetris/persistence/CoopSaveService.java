package kr.ac.jbnu.se.tetris.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import kr.ac.jbnu.se.tetris.network.coop.CoopSnapshot;

/** 온라인 협동 공동 저장 (coopId 본문 + 유저별 포인터). */
public final class CoopSaveService {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path COOP_DIR = Paths.get("data", "coop");
	private static final Path USER_COOP_DIR = Paths.get("data", "saves", "coop_slots");

	private CoopSaveService() {
	}

	public static void save(CoopSnapshot snapshot, String userId, int slot) throws IOException {
		if (snapshot == null || snapshot.coopId == null || snapshot.coopId.isEmpty()) {
			throw new IOException("coopId가 없어요.");
		}
		Files.createDirectories(COOP_DIR);
		Path body = COOP_DIR.resolve(snapshot.coopId + ".json");
		Path tmp = COOP_DIR.resolve(snapshot.coopId + ".json.tmp");
		Files.writeString(tmp, GSON.toJson(snapshot), StandardCharsets.UTF_8);
		Files.move(tmp, body, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);

		if (userId != null && !userId.isEmpty() && slot >= 1 && slot <= 3) {
			Files.createDirectories(USER_COOP_DIR.resolve(userId));
			Path slotFile = USER_COOP_DIR.resolve(userId).resolve("slot" + slot + ".txt");
			Files.writeString(slotFile, snapshot.coopId, StandardCharsets.UTF_8);
		}
	}

	public static CoopSnapshot loadByCoopId(String coopId) throws IOException {
		Path body = COOP_DIR.resolve(coopId + ".json");
		if (!Files.isRegularFile(body)) {
			throw new IOException("저장 파일을 찾을 수 없어요.");
		}
		return GSON.fromJson(Files.readString(body, StandardCharsets.UTF_8), CoopSnapshot.class);
	}

	public static String coopIdForUserSlot(String userId, int slot) throws IOException {
		Path slotFile = USER_COOP_DIR.resolve(userId).resolve("slot" + slot + ".txt");
		if (!Files.isRegularFile(slotFile)) {
			return null;
		}
		return Files.readString(slotFile, StandardCharsets.UTF_8).trim();
	}
}
