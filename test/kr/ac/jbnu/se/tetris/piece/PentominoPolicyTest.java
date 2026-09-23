package kr.ac.jbnu.se.tetris.piece;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PentominoPolicyTest {

	@Test
	void stage12NoPentominoInManyBags() {
		PentominoPolicy pol = new PentominoPolicy();
		pol.probability = 1.0;
		SevenBagRandomizer bag = new SevenBagRandomizer(100L, pol);
		bag.setStage(1);
		for (int i = 0; i < 20; i++)
			bag.next();
		assertFalse(bag.queueContainsPentomino());
	}

	@Test
	void stage3InsertsPentominoAndKeepsSevenTetrominoTypes() {
		PentominoPolicy pol = new PentominoPolicy();
		pol.probability = 1.0;
		SevenBagRandomizer bag = new SevenBagRandomizer(200L, pol);
		bag.setStage(3);
		int before = bag.queue.size();
		bag.appendOneBag();
		assertTrue(bag.queue.size() == before + 8);
		assertTrue(bag.queueContainsPentomino());
		ArrayList<Tetrominoes> slice = new ArrayList<>();
		for (int i = before; i < bag.queue.size(); i++)
			slice.add(bag.queue.get(i));
		assertTrue(SevenBagRandomizer.hasAllTetrominoes(slice));
	}
}
