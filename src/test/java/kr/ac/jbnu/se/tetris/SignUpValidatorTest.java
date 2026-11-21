package kr.ac.jbnu.se.tetris;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class SignUpValidatorTest {

    private static final String NICK_MSG = "닉네임은 한글·영문·숫자 2~10자로 입력해 주세요.";
    private static final String PW_LEN_MSG = "비밀번호는 8~64자로 입력해 주세요.";

    private String validate(String nick, String pw) {
        return SignUpValidator.validate(nick, "a@b.com", pw, pw);
    }

    @Test
    void nicknameBoundaries() {
        assertEquals(NICK_MSG, validate("a", "12345678"));
        assertNull(validate("ab", "12345678"));
        assertNull(validate("a".repeat(10), "12345678"));
        assertEquals(NICK_MSG, validate("a".repeat(11), "12345678"));
    }

    @Test
    void nicknameAllowsKoreanAndRejectsSymbols() {
        assertNull(validate("용사님1", "12345678"));
        assertEquals(NICK_MSG, validate("ab cd", "12345678"));
        assertEquals(NICK_MSG, validate("ab,cd", "12345678"));
    }

    @Test
    void passwordLengthBoundaries() {
        assertEquals(PW_LEN_MSG, validate("abc", "a".repeat(7)));
        assertNull(validate("abc", "a".repeat(8)));
        assertNull(validate("abc", "a".repeat(64)));
        assertEquals(PW_LEN_MSG, validate("abc", "a".repeat(65)));
    }

    @Test
    void passwordRejectsCommaAndMismatch() {
        assertEquals("비밀번호에 쉼표(,)는 쓸 수 없어요.", validate("abc", "1234567,8"));
        assertEquals("비밀번호 확인이 일치하지 않아요.",
                SignUpValidator.validate("abc", "a@b.com", "12345678", "12345679"));
    }

    @Test
    void emailFormat() {
        String msg = "이메일 형식이 올바르지 않아요.";
        assertEquals(msg, SignUpValidator.validate("abc", "ab.com", "12345678", "12345678"));
        assertEquals(msg, SignUpValidator.validate("abc", "a@b", "12345678", "12345678"));
        assertEquals(msg, SignUpValidator.validate("abc", "a,b@c.com", "12345678", "12345678"));
        assertNull(SignUpValidator.validate("abc", "a@b.co", "12345678", "12345678"));
    }

    @Test
    void checksRunInOriginalOrder() {
        assertEquals(NICK_MSG, SignUpValidator.validate("a", "bad", "1", "2"));
    }

    @Test
    void loginInputMustNotBeEmpty() {
        String msg = "닉네임과 비밀번호를 모두 입력해 주세요.";
        assertEquals(msg, SignUpValidator.validateLoginInput("", "pw"));
        assertEquals(msg, SignUpValidator.validateLoginInput("nick", ""));
        assertNull(SignUpValidator.validateLoginInput("nick", "pw"));
    }
}
