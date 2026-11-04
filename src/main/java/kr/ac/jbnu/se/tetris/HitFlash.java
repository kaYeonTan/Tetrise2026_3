package kr.ac.jbnu.se.tetris;

/** 보스 피격 연출(번쩍임·HP바 흔들림)의 프레임 규칙. 남은 프레임 수는 GamePanel.hitEffect가 들고 있다. */
final class HitFlash {
    static final int DURATION_FRAMES = 12;

    private HitFlash() {
    }

    /** 남은 프레임을 4로 나눈 나머지가 0·1일 때만 보여, 2프레임 켜지고 2프레임 꺼지는 번쩍임 */
    static boolean flashVisible(int framesLeft) {
        return framesLeft > 0 && framesLeft % 4 < 2;
    }

    /** HP바가 프레임마다 좌우로 3px씩 흔들리는 오프셋 */
    static int shakeOffset(int framesLeft) {
        if (framesLeft <= 0) {
            return 0;
        }
        return framesLeft % 2 == 0 ? 3 : -3;
    }
}
