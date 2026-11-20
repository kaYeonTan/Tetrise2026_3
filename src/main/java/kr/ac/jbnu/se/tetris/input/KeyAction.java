package kr.ac.jbnu.se.tetris.input;

/** 키로 조작하는 동작. 선언 순서가 기존 키 배열의 인덱스(0~6)와 같다. */
public enum KeyAction {
    LEFT("왼쪽 이동"),
    RIGHT("오른쪽 이동"),
    SOFT_DROP("천천히 내리기"),
    HARD_DROP("바로 떨어뜨리기"),
    ROTATE_CW("시계 방향 회전"),
    ROTATE_CCW("반시계 방향 회전"),
    HOLD("홀드");

    private final String displayName;

    KeyAction(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
