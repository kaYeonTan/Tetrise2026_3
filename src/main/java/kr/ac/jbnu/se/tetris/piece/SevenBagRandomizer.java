package kr.ac.jbnu.se.tetris.piece;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import kr.ac.jbnu.se.tetris.Shape;
import kr.ac.jbnu.se.tetris.Tetrominoes;

// F-02 7-Bag + F-04 펜토 삽입
public class SevenBagRandomizer implements Randomizer {

	public static final int PREVIEW_COUNT = 3;

	static final Tetrominoes[] POOL = { Tetrominoes.ZShape, Tetrominoes.SShape, Tetrominoes.LineShape,
			Tetrominoes.TShape, Tetrominoes.SquareShape, Tetrominoes.LShape, Tetrominoes.MirroredLShape };

	long seed;
	ReplayRandom r;
	List<Tetrominoes> queue = new ArrayList<>();
	PentominoPolicy policy;
	int stage = 1;

	public SevenBagRandomizer(long seed, PentominoPolicy policy) {
		this.seed = seed;
		this.policy = policy != null ? policy : PentominoPolicy.defaults();
		r = new ReplayRandom(seed);
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
		Collections.addAll(bag, POOL);
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
		Tetrominoes[] order = queue.toArray(new Tetrominoes[0]);
		return new RandomizerState(seed, r.steps(), order);
	}

	@Override
	public void restoreState(RandomizerState state) {
		if (state == null) {
			return;
		}
		seed = state.seed;
		ReplayRandom restored = new ReplayRandom(seed);
		restored.advance(Math.max(0, state.index));
		r = restored;
		queue.clear();
		if (state.bagOrder == null) {
			return;
		}
		Collections.addAll(queue, state.bagOrder);
	}

	// 테스트용: 큐에 펜토 있는지
	public boolean queueContainsPentomino() {
		for (int i = 0; i < queue.size(); i++) {
			if (Shape.isPentomino(queue.get(i)))
				return true;
		}
		return false;
	}

	/**
	 * java.util.Random과 같은 수열을 유지하면서 next 호출 횟수만 기억한다.
	 * 시드를 다시 넣고 같은 횟수만큼 진행하면 이어하기 뒤 가방이 같다.
	 */
	static final class ReplayRandom extends Random {
		private int steps;

		ReplayRandom(long seed) {
			super(seed);
		}

		int steps() {
			return steps;
		}

		void advance(int target) {
			while (steps < target) {
				super.next(32);
				steps++;
			}
		}

		@Override
		protected int next(int bits) {
			steps++;
			return super.next(bits);
		}
	}

	// bag 한 세트에 테트로미노 7종 다 있는지
	public static boolean hasAllTetrominoes(List<Tetrominoes> bag) {
		HashSet<Tetrominoes> set = new HashSet<>();
		for (Tetrominoes t : bag) {
			if (!Shape.isPentomino(t))
				set.add(t);
		}
		return set.size() == POOL.length;
	}
}
