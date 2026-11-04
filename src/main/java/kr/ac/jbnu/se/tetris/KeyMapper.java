package kr.ac.jbnu.se.tetris;

import java.util.Optional;

import kr.ac.jbnu.se.tetris.core.PlayerAction;

/** 키 코드 → PlayerAction 매핑 (Command 패턴의 입력 변환 단계). 키 배열 순서는 설정 화면과 동일하다. */
public final class KeyMapper {
    private static final PlayerAction[] ORDER = {
        PlayerAction.MOVE_LEFT, PlayerAction.MOVE_RIGHT, PlayerAction.SOFT_DROP, PlayerAction.HARD_DROP,
        PlayerAction.ROTATE_CW, PlayerAction.ROTATE_CCW, PlayerAction.HOLD
    };

    private KeyMapper() {
    }

    public static Optional<PlayerAction> map(int code, int[] keys) {
        for (int i = 0; i < ORDER.length && i < keys.length; i++) {
            if (keys[i] == code) {
                return Optional.of(ORDER[i]);
            }
        }
        return Optional.empty();
    }
}
