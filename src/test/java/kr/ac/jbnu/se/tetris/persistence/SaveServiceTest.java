package kr.ac.jbnu.se.tetris.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

class SaveServiceTest {

	@Test
	void saveAndLoadCoopSlot(@TempDir Path temp) throws Exception {
		System.setProperty("user.dir", temp.toString());
		CoopGameMemento m = new CoopGameMemento();
		m.coopId = "test-coop-id";
		m.seed = 42L;
		m.stage = 2;
		m.bossHp = 80;
		m.bossMax = 150;
		SaveService.saveCoop(null, 2, m);
		CoopGameMemento loaded = SaveService.loadCoop(null, 2);
		assertEquals(m.coopId, loaded.coopId);
		assertEquals(2, loaded.stage);
		assertTrue(SaveService.listCoopSlots(null).get(1).occupied);
	}
}
