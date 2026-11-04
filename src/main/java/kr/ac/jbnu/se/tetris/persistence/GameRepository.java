package kr.ac.jbnu.se.tetris.persistence;

import java.io.IOException;
import java.util.List;

/** Storage abstraction for save slots; callers do not know the file format or location. */
public interface GameRepository {

    void save(String userId, int slot, CoopGameMemento memento) throws IOException;

    CoopGameMemento load(String userId, int slot) throws LoadException;

    List<SaveSlotMeta> list(String userId);

    void delete(String userId, int slot) throws IOException;

    boolean allSlotsOccupied(String userId);
}
