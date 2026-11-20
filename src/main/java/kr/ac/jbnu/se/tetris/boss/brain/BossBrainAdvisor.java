package kr.ac.jbnu.se.tetris.boss.brain;

import java.util.function.Function;
import java.util.function.Supplier;
import kr.ac.jbnu.se.tetris.core.GameSession;
import kr.ac.jbnu.se.tetris.integration.ASinglePlayerBridge;

public final class BossBrainAdvisor {
    private BossBrainAdvisor() {}
    public static <T> T select(boolean useEngine, ASinglePlayerBridge bridge, Supplier<T> fallback,
                               Function<BossBrainRunner.Decision, T> cached,
                               Function<GameSession, T> calculate) {
        GameSession session = useEngine ? bridge.session() : null;
        if (session == null) return fallback.get();
        BossBrainRunner.Decision decision = bridge.brainDecision();
        if (decision != null && decision.responsePiece() == bridge.currentShape()) return cached.apply(decision);
        return calculate.apply(session);
    }
}
