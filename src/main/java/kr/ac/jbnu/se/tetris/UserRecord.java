package kr.ac.jbnu.se.tetris;

/** users.txt 한 줄(닉네임,이메일,비밀번호)과 객체 사이의 변환을 한 곳에 모은다. */
public record UserRecord(String nick, String email, String password) {

    public UserRecord {
        if (nick == null || email == null || password == null) {
            throw new IllegalArgumentException("user fields must not be null");
        }
    }

    /** 형식이 맞지 않는 줄은 null. */
    public static UserRecord fromLine(String line) {
        String[] data = line.split(",");
        if (data.length < 3) {
            return null;
        }
        return new UserRecord(data[0], data[1], data[2]);
    }

    public String toLine() {
        return nick + "," + email + "," + password;
    }
}
