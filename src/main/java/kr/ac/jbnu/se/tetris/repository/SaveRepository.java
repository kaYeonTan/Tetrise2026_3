package kr.ac.jbnu.se.tetris.repository;

import java.util.ArrayList;

import kr.ac.jbnu.se.tetris.GamePanel;

/** 게임 저장 슬롯 저장소. */
public interface SaveRepository {

    boolean saveGame(GamePanel p);

    boolean loadGame(String path, GamePanel p);

    /** 저장목록 {파일경로, 날짜, 스테이지, 점수, 인원}, 최신순. */
    ArrayList<String[]> getSaveList(String nickname);

    void deleteSave(String path);
}
