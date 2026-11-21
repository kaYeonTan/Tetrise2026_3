package kr.ac.jbnu.se.tetris.repository;

/** 키 설정 저장소. 저장은 지금 적용 중인 키를, 불러오기는 읽은 키를 게임에 바로 적용한다. */
public interface KeyBindingRepository {

    void saveKeys();

    void loadKeys();
}
