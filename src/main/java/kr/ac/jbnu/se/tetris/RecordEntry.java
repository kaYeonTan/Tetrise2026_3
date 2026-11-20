package kr.ac.jbnu.se.tetris;

/** records.txt 한 줄(닉네임,점수,스테이지,인원,클리어,시각)을 담는 값 객체. */
public record RecordEntry(String nick, int score, int stage, int players, boolean clear, long timeMillis) {

    private static final int FIELD_COUNT = 6;

    /** 형식이 맞지 않거나 숫자가 아니면 null. */
    public static RecordEntry fromLine(String line) {
        String[] data = line.split(",");
        if (data.length != FIELD_COUNT) {
            return null;
        }
        try {
            return new RecordEntry(data[0], Integer.parseInt(data[1]), Integer.parseInt(data[2]),
                    Integer.parseInt(data[3]), "1".equals(data[4]), Long.parseLong(data[5]));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String toLine() {
        return nick + "," + score + "," + stage + "," + players + "," + (clear ? 1 : 0) + "," + timeMillis;
    }

    /** 스테이지 랭킹 비교값. 전체 클리어가 같은 스테이지의 미클리어보다 앞선다. */
    public int progress() {
        return stage * 10 + (clear ? 1 : 0);
    }
}
