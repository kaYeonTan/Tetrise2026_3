package kr.ac.jbnu.se.tetris.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GameEventBusTest {

	@Test
	void typedSubscriberGetsPublish() {
		GameEventBus bus = new GameEventBus();
		final int[] got = { 0 };

		bus.subscribe(GameEventType.LINES_CLEARED, e -> {
			if (e.value == 2 && e.playerIndex == 0)
				got[0] = 1;
		});

		bus.publish(new GameEvent(GameEventType.LINES_CLEARED, 0, 2));

		assertEquals(1, got[0]);
		assertTrue(bus.getPublishCount() >= 1);
	}
}
