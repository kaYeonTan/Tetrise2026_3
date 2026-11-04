package kr.ac.jbnu.se.tetris.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

class InMemoryGameRepositoryTest {

	private final GameRepository repository = new InMemoryGameRepository();

	private static CoopGameMemento memento(int stage) {
		CoopGameMemento m = new CoopGameMemento();
		m.coopId = "mem-test";
		m.stage = stage;
		return m;
	}

	@Test
	void saveThenLoadReturnsIndependentCopy() throws Exception {
		CoopGameMemento original = memento(2);
		repository.save("hostA", 1, original);
		original.stage = 5;

		CoopGameMemento loaded = repository.load("hostA", 1);
		assertEquals(2, loaded.stage);
		assertTrue(repository.list("hostA").get(0).occupied);
		assertFalse(repository.list("hostA").get(1).occupied);
	}

	@Test
	void loadOfEmptyOrInvalidSlotFails() {
		assertThrows(LoadException.class, () -> repository.load("hostA", 2));
		assertThrows(LoadException.class, () -> repository.load("hostA", 9));
		assertThrows(IOException.class, () -> repository.save("hostA", 0, memento(1)));
	}

	@Test
	void deleteAndAllSlotsOccupiedTrackState() throws Exception {
		for (int slot = SaveService.MIN_SLOT; slot <= SaveService.MAX_SLOT; slot++) {
			repository.save(null, slot, memento(slot));
		}
		assertTrue(repository.allSlotsOccupied(null));
		repository.delete(null, 2);
		assertFalse(repository.allSlotsOccupied(null));
	}
}
