package kr.ac.jbnu.se.tetris.persistence;

import java.io.IOException;
import java.util.List;

/** JSON file backed repository; delegates to {@link SaveService}. */
public final class JsonGameRepository implements GameRepository {

    @Override
    public void save(String userId, int slot, CoopGameMemento memento) throws IOException {
        SaveService.saveCoop(userId, slot, memento);
    }

    @Override
    public CoopGameMemento load(String userId, int slot) throws LoadException {
        return SaveService.loadCoop(userId, slot);
    }

    @Override
    public List<SaveSlotMeta> list(String userId) {
        return SaveService.listCoopSlots(userId);
    }

    @Override
    public void delete(String userId, int slot) throws IOException {
        SaveService.deleteSlot(userId, slot);
    }

    @Override
    public boolean allSlotsOccupied(String userId) {
        return SaveService.allCoopSlotsOccupied(userId);
    }
}
