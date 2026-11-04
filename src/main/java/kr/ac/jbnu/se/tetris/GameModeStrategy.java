package kr.ac.jbnu.se.tetris;

/** 게임 모드(1인/로컬 2인/온라인 협동)별로 달라지는 규칙 (Strategy 패턴). */
public interface GameModeStrategy {

    boolean controlsPlayer(int playerIndex, int localPlayerIndex);

    boolean runsLocalGravity();

    /** 주 플레이어가 쓰는 기본 키 배열 (임시 키 매핑 적용 전). */
    int[] primaryKeys(int[] singleKeys, int[] player1Keys);

    GameModeStrategy SINGLE = new GameModeStrategy() {
        @Override
        public boolean controlsPlayer(int playerIndex, int localPlayerIndex) {
            return playerIndex == 0;
        }

        @Override
        public boolean runsLocalGravity() {
            return true;
        }

        @Override
        public int[] primaryKeys(int[] singleKeys, int[] player1Keys) {
            return singleKeys;
        }
    };

    GameModeStrategy LOCAL_COOP = new GameModeStrategy() {
        @Override
        public boolean controlsPlayer(int playerIndex, int localPlayerIndex) {
            return playerIndex == 0 || playerIndex == 1;
        }

        @Override
        public boolean runsLocalGravity() {
            return true;
        }

        @Override
        public int[] primaryKeys(int[] singleKeys, int[] player1Keys) {
            return player1Keys;
        }
    };

    GameModeStrategy ONLINE_COOP = new GameModeStrategy() {
        @Override
        public boolean controlsPlayer(int playerIndex, int localPlayerIndex) {
            return localPlayerIndex == playerIndex;
        }

        @Override
        public boolean runsLocalGravity() {
            return false;
        }

        @Override
        public int[] primaryKeys(int[] singleKeys, int[] player1Keys) {
            return singleKeys;
        }
    };

    static GameModeStrategy of(GameContext ctx) {
        return of(ctx.onlineCoop(), ctx.isTwoPlayerMode());
    }

    static GameModeStrategy of(boolean onlineCoop, boolean twoPlayer) {
        if (onlineCoop) {
            return ONLINE_COOP;
        }
        return twoPlayer ? LOCAL_COOP : SINGLE;
    }
}
