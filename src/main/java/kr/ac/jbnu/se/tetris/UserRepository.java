package kr.ac.jbnu.se.tetris;

import java.util.List;

/** 사용자 계정 저장소. 저장 방식이 바뀌어도 AccountService의 규칙은 그대로 둔다. */
public interface UserRepository {

    List<UserRecord> findAll();

    /** 저장에 성공하면 true. */
    boolean add(UserRecord user);

    /** 닉네임(대소문자 무시)의 저장된 비밀번호 값을 바꾼다. 해당 계정이 없거나 저장에 실패하면 false. */
    boolean replacePassword(String nick, String storedPassword);
}
