package kr.ac.jbnu.se.tetris.persistence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Memory-only repository for tests; touches no files. */
public final class InMemoryGameRepository implements GameRepository {

    private final Map<String, CoopGameMemento> store = new HashMap<>();

    @Override
    public void save(String userId, int slot, CoopGameMemento memento) throws IOException {
        checkSlot(slot);
        if (memento == null) {
            throw new IOException("저장할 데이터가 없어요.");
        }
        memento.savedAtMs = System.currentTimeMillis();
        store.put(key(userId, slot), SaveService.copyCoopMemento(memento));
    }

    @Override
    public CoopGameMemento load(String userId, int slot) throws LoadException {
        if (slot < SaveService.MIN_SLOT || slot > SaveService.MAX_SLOT) {
            throw new LoadException("슬롯 번호가 올바르지 않아요.");
        }
        CoopGameMemento stored = store.get(key(userId, slot));
        if (stored == null) {
            throw new LoadException("비어 있는 슬롯이에요.");
        }
        SaveService.assertCanLoadCoop(stored, userId);
        return SaveService.copyCoopMemento(stored);
    }

    @Override
    public List<SaveSlotMeta> list(String userId) {
        List<SaveSlotMeta> out = new ArrayList<>();
        for (int s = SaveService.MIN_SLOT; s <= SaveService.MAX_SLOT; s++) {
            CoopGameMemento m = store.get(key(userId, s));
            if (m == null) {
                out.add(new SaveSlotMeta(s, false, "비어 있음", 0));
            } else {
                out.add(new SaveSlotMeta(s, true, SaveService.formatCoopSlotSummary(m, userId, s), m.savedAtMs));
            }
        }
        return out;
    }

    @Override
    public void delete(String userId, int slot) {
        store.remove(key(userId, slot));
    }

    @Override
    public boolean allSlotsOccupied(String userId) {
        for (int s = SaveService.MIN_SLOT; s <= SaveService.MAX_SLOT; s++) {
            if (!store.containsKey(key(userId, s))) {
                return false;
            }
        }
        return true;
    }

    private static void checkSlot(int slot) throws IOException {
        if (slot < SaveService.MIN_SLOT || slot > SaveService.MAX_SLOT) {
            throw new IOException("슬롯은 1~3만 사용할 수 있어요.");
        }
    }

    private static String key(String userId, int slot) {
        return SaveService.storageUserId(userId) + "#" + slot;
    }
}
