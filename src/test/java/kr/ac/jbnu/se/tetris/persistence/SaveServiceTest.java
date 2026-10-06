package kr.ac.jbnu.se.tetris.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

	@Test
	void onlineCoopLoadRequiresMatchingAccount(@TempDir Path temp) throws Exception {
		System.setProperty("user.dir", temp.toString());
		CoopGameMemento m = new CoopGameMemento();
		m.coopId = "pair-coop";
		m.onlineResume = true;
		m.hostAccountId = "hostA";
		m.guestAccountId = "guestB";
		m.stage = 3;
		SessionMemento p0 = new SessionMemento();
		p0.score = 1000;
		SessionMemento p1 = new SessionMemento();
		p1.score = 2000;
		m.player0 = p0;
		m.player1 = p1;
		SaveService.saveCoop("hostA", 1, m);

		CoopGameMemento forHost = SaveService.loadCoop("hostA", 1);
		assertEquals("guestB", SaveService.coopPartnerNick(forHost, "hostA"));

		CoopGameMemento onDisk = SaveService.readCoopSlot("hostA", 1);
		LoadException denied = assertThrows(LoadException.class,
				() -> SaveService.assertCanLoadCoop(onDisk, "stranger"));
		assertTrue(denied.getMessage().contains("두 계정"));

		String summary = SaveService.formatCoopSlotSummary(forHost, "hostA", 1);
		assertTrue(summary.contains("guestB"));
		assertTrue(summary.contains("3,000"));
	}

	@Test
	void remapSwapsBoardsWhenHostAndGuestFlip(@TempDir Path temp) {
		System.setProperty("user.dir", temp.toString());
		CoopGameMemento m = new CoopGameMemento();
		m.coopId = "flip";
		m.hostAccountId = "A";
		m.guestAccountId = "B";
		SessionMemento hostBoard = new SessionMemento();
		hostBoard.score = 111;
		SessionMemento guestBoard = new SessionMemento();
		guestBoard.score = 222;
		m.player0 = hostBoard;
		m.player1 = guestBoard;

		CoopGameMemento remapped = SaveService.remapForCurrentOnlineHost(m, "B", "A");
		assertEquals("B", remapped.hostAccountId);
		assertEquals("A", remapped.guestAccountId);
		assertEquals(222, remapped.player0.score);
		assertEquals(111, remapped.player1.score);
	}
}
