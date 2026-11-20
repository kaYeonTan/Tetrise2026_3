package kr.ac.jbnu.se.tetris;

import java.util.regex.Pattern;

/** 회원가입 입력값 규칙. 화면(LoginFrame) 없이 테스트할 수 있도록 분리했다. */
public final class SignUpValidator {

    public static final int PASSWORD_MIN = 8;
    public static final int PASSWORD_MAX = 64;

    private static final Pattern NICKNAME = Pattern.compile("^[가-힣A-Za-z0-9]{2,10}$");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s,]+@[^@\\s,]+\\.[^@\\s,]+$");

    private SignUpValidator() {
    }

    /** 규칙을 모두 만족하면 null, 아니면 화면에 보여줄 안내 문구를 돌려준다. 검사 순서는 1차와 같다. */
    public static String validate(String nickname, String email, String password, String passwordConfirm) {
        if (!NICKNAME.matcher(nickname).matches()) {
            return "닉네임은 한글·영문·숫자 2~10자로 입력해 주세요.";
        }
        if (!EMAIL.matcher(email).matches()) {
            return "이메일 형식이 올바르지 않아요.";
        }
        if (password.length() < PASSWORD_MIN || password.length() > PASSWORD_MAX) {
            return "비밀번호는 8~64자로 입력해 주세요.";
        }
        if (password.contains(",")) {
            return "비밀번호에 쉼표(,)는 쓸 수 없어요.";
        }
        if (!password.equals(passwordConfirm)) {
            return "비밀번호 확인이 일치하지 않아요.";
        }
        return null;
    }

    /** 로그인 입력이 비어 있으면 안내 문구, 아니면 null. */
    public static String validateLoginInput(String nickname, String password) {
        if (nickname.isEmpty() || password.isEmpty()) {
            return "닉네임과 비밀번호를 모두 입력해 주세요.";
        }
        return null;
    }
}
