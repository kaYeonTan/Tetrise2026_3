package kr.ac.jbnu.se.tetris.piece;

import org.junit.jupiter.api.Test;

import kr.ac.jbnu.se.tetris.Tetrominoes;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SevenBagRandomizerTest {

	@Test
	void sevenInRowNoDuplicate() {
		SevenBagRandomizer bag = new SevenBagRandomizer(12345L);
		HashSet<Tetrominoes> set = new HashSet<>();
		for (int i = 0; i < 7; i++)
			set.add(bag.next());
		assertEquals(7, set.size());
	}

	@Test
	void sameSeedSameOrder() {
		SevenBagRandomizer a = new SevenBagRandomizer(999L);
		SevenBagRandomizer b = new SevenBagRandomizer(999L);
		for (int i = 0; i < 14; i++)
			assertEquals(a.next(), b.next());
	}

	@Test
	void peekMatchesNext() {
		SevenBagRandomizer bag = new SevenBagRandomizer(42L);
		Tetrominoes peek = bag.peekNext();
		Tetrominoes got = bag.next();
		assertEquals(peek, got);
	}

	@Test
	void peekThreeAhead() {
		SevenBagRandomizer bag = new SevenBagRandomizer(777L);
		Tetrominoes a = bag.peekAt(0);
		Tetrominoes b = bag.peekAt(1);
		Tetrominoes c = bag.peekAt(2);
		assertEquals(a, bag.next());
		assertEquals(b, bag.next());
		assertEquals(c, bag.next());
	}
}
