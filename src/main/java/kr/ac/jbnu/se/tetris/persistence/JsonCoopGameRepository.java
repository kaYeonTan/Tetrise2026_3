package kr.ac.jbnu.se.tetris.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

public final class JsonCoopGameRepository implements CoopGameRepository {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	@Override
	public void saveCoop(String loginUser, int slot, CoopGameMemento memento) throws IOException {
		if (slot < SaveService.MIN_SLOT || slot > SaveService.MAX_SLOT) {
			throw new IOException("슬롯은 1~3만 사용할 수 있어요.");
		}
		if (memento == null || memento.coopId == null || memento.coopId.isEmpty()) {
			throw new IOException("coopId가 없어요.");
		}
		memento.savedAtMs = System.currentTimeMillis();
		String userId = SaveService.storageUserId(loginUser);
		Path dir = Paths.get("data", "saves", userId);
		Files.createDirectories(dir);
		Path target = SaveService.slotPath(userId, slot);
		Path tmp = dir.resolve("slot" + slot + ".json.tmp");
		Files.writeString(tmp, GSON.toJson(memento), StandardCharsets.UTF_8);
		Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);

		Path coopBody = Paths.get("data", "coop", memento.coopId + ".json");
		Files.createDirectories(coopBody.getParent());
		Path coopTmp = Paths.get("data", "coop", memento.coopId + ".json.tmp");
		Files.writeString(coopTmp, GSON.toJson(memento), StandardCharsets.UTF_8);
		Files.move(coopTmp, coopBody, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
	}

	@Override
	public CoopGameMemento readCoopSlot(String loginUser, int slot) throws LoadException {
		if (slot < SaveService.MIN_SLOT || slot > SaveService.MAX_SLOT) {
			throw new LoadException("슬롯 번호가 올바르지 않아요.");
		}
		Path path = SaveService.slotPath(SaveService.storageUserId(loginUser), slot);
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

	@Override
	public void deleteSlot(String loginUser, int slot) throws IOException {
		Path path = SaveService.slotPath(SaveService.storageUserId(loginUser), slot);
		Files.deleteIfExists(path);
	}

	@Override
	public CoopGameMemento copyMemento(CoopGameMemento m) {
		if (m == null) {
			return null;
		}
		return GSON.fromJson(GSON.toJson(m), CoopGameMemento.class);
	}
}
