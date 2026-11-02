package kr.ac.jbnu.se.tetris.persistence;

import java.io.IOException;

public interface CoopGameRepository {

	void saveCoop(String loginUser, int slot, CoopGameMemento memento) throws IOException;

	CoopGameMemento readCoopSlot(String loginUser, int slot) throws LoadException;

	void deleteSlot(String loginUser, int slot) throws IOException;

	CoopGameMemento copyMemento(CoopGameMemento m);
}
