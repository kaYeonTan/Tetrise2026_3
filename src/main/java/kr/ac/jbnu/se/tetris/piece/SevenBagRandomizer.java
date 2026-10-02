package kr.ac.jbnu.se.tetris.piece;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

import kr.ac.jbnu.se.tetris.Shape;
import kr.ac.jbnu.se.tetris.Tetrominoes;

// F-02 7-Bag + F-04 펜토 삽입
public class SevenBagRandomizer implements Randomizer {

	public static final int PREVIEW_COUNT = 3;

	static final Tetrominoes[] POOL = { Tetrominoes.ZShape, Tetrominoes.SShape, Tetrominoes.LineShape,
			Tetrominoes.TShape, Tetrominoes.SquareShape, Tetrominoes.LShape, Tetrominoes.MirroredLShape };

	long seed;
	Random r;
	ArrayList<Tetrominoes> queue = new ArrayList<>();
	PentominoPolicy policy;
	int stage = 1;

	public SevenBagRandomizer(long seed, PentominoPolicy policy) {
		this.seed = seed;
		this.policy = policy != null ? policy : PentominoPolicy.defaults();
		r = new Random(seed);
		ensureQueue(7);
	}

	public SevenBagRandomizer(long seed) {
		this(seed, PentominoPolicy.defaults());
	}

	public SevenBagRandomizer() {
		this(System.currentTimeMillis());
	}

	public void setStage(int stage) {
		this.stage = stage;
	}

	void appendOneBag() {
		ArrayList<Tetrominoes> bag = new ArrayList<>();
		for (int i = 0; i < POOL.length; i++)
			bag.add(POOL[i]);
		Collections.shuffle(bag, r);

		// AC-04-3 테트로미노 7종 유지 + 펜토 1개 끼워넣기
		if (policy.shouldInsert(stage, r)) {
			Tetrominoes p = policy.randomPentomino(r);
			int pos = r.nextInt(bag.size() + 1);
			bag.add(pos, p);
		}

		queue.addAll(bag);
	}

	void ensureQueue(int need) {
		while (queue.size() < need)
			appendOneBag();
	}

	@Override
	public Tetrominoes next() {
		ensureQueue(1);
		return queue.remove(0);
	}

	@Override
	public Tetrominoes peekNext() {
		return peekAt(0);
	}

	public Tetrominoes peekAt(int offset) {
		if (offset < 0)
			return Tetrominoes.NoShape;
		ensureQueue(offset + 1);
		return queue.get(offset);
	}

	@Override
	public long getSeed() {
		return seed;
	}

	@Override
	public RandomizerState saveState() {
		Tetrominoes[] order = new Tetrominoes[queue.size()];
		for (int i = 0; i < queue.size(); i++)
			order[i] = queue.get(i);
		return new RandomizerState(seed, 0, order);
	}

	@Override
	public void restoreState(RandomizerState state) {
		seed = state.seed;
		r = new Random(seed);
		queue.clear();
		for (int i = 0; i < state.bagOrder.length; i++)
			queue.add(state.bagOrder[i]);
	}

	// 테스트용: 큐에 펜토 있는지
	public boolean queueContainsPentomino() {
		for (int i = 0; i < queue.size(); i++) {
			if (Shape.isPentomino(queue.get(i)))
				return true;
		}
		return false;
	}

	// bag 한 세트에 테트로미노 7종 다 있는지
	public static boolean hasAllTetrominoes(ArrayList<Tetrominoes> bag) {
		HashSet<Tetrominoes> set = new HashSet<>();
		for (Tetrominoes t : bag) {
			if (!Shape.isPentomino(t))
				set.add(t);
		}
		return set.size() == POOL.length;
	}
}
