package kr.ac.jbnu.se.tetris.boss.brain;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import kr.ac.jbnu.se.tetris.Tetrominoes;
import kr.ac.jbnu.se.tetris.core.GameEngine;
import kr.ac.jbnu.se.tetris.core.GameSession;
import kr.ac.jbnu.se.tetris.event.GameEventType;

/**
 * Keeps a fresh immutable board snapshot off the game loop after every lock.
 * Only the newest snapshot is searched while the worker is busy.
 */
public final class BossBrainRunner implements AutoCloseable {

    private final ExecutorService worker = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "boss-brain");
        thread.setDaemon(true);
        return thread;
    });
    private final AtomicReference<Grid> latestGrid = new AtomicReference<>(Grid.empty());
    private final AtomicReference<Decision> latestDecision = new AtomicReference<>();
    private final AtomicReference<SearchRequest> pendingRequest = new AtomicReference<>();
    private final AtomicBoolean searchScheduled = new AtomicBoolean();
    private final AtomicBoolean closed = new AtomicBoolean();
    private final MaxMinBrain brain = new MaxMinBrain();

    public BossBrainRunner(GameEngine engine) {
        latestGrid.set(Grid.from(engine.getSession(0)));
        engine.getBus().subscribe(GameEventType.PIECE_LOCKED, event -> submitCurrentBoard(engine));
    }

    public Grid latestGrid() {
        return latestGrid.get();
    }

    public void refresh(GameEngine engine) {
        submitCurrentBoard(engine);
    }

    private void submitCurrentBoard(GameEngine engine) {
        GameSession session = engine.getSession(0);
        submit(Grid.from(session), session.peekNextBlock());
    }

    public Decision latestDecision() {
        return latestDecision.get();
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            pendingRequest.set(null);
            worker.shutdownNow();
        }
    }

    private void submit(Grid snapshot, Tetrominoes responsePiece) {
        if (closed.get()) {
            return;
        }
        latestGrid.set(snapshot);
        latestDecision.set(null);
        pendingRequest.set(new SearchRequest(snapshot, responsePiece));
        if (!searchScheduled.compareAndSet(false, true)) {
            return;
        }
        try {
            worker.execute(this::searchLatest);
        } catch (RejectedExecutionException exception) {
            searchScheduled.set(false);
            if (!closed.get()) {
                throw exception;
            }
        }
    }

    private void searchLatest() {
        while (!closed.get()) {
            SearchRequest request = pendingRequest.getAndSet(null);
            if (request == null) {
                searchScheduled.set(false);
                if (pendingRequest.get() == null || !searchScheduled.compareAndSet(false, true)) {
                    return;
                }
                continue;
            }
            latestDecision.set(new Decision(request.responsePiece(),
                    brain.chooseGarbageHole(request.grid(), request.responsePiece()),
                    brain.chooseForcedPiece(request.grid())));
        }
        searchScheduled.set(false);
    }

    public record Decision(Tetrominoes responsePiece, int garbageHole, Tetrominoes forcedPiece) {
    }

    private record SearchRequest(Grid grid, Tetrominoes responsePiece) {
    }
}
