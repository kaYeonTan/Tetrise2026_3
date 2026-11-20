package kr.ac.jbnu.se.tetris;

/** 보스 피격 연출(번쩍임·HP바 흔들림)의 프레임 규칙. 남은 프레임 수와 표시 규칙을 관리한다. */
final class HitFlash {

    static final int DURATION = 12;
    private int framesLeft;

    HitFlash() {}
    void start() { framesLeft = DURATION + (500 + 15) / 16; }
    void tick() { if (framesLeft > 0) framesLeft--; }
    boolean isVisible() { return flashVisible(framesLeft); }
    int shakeOffset() { return shakeOffset(framesLeft); }
    int get() { return framesLeft; }
    void set(int value) { framesLeft = value; }

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
