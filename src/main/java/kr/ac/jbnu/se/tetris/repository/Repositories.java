package kr.ac.jbnu.se.tetris.repository;

import java.nio.file.Path;

import kr.ac.jbnu.se.tetris.AccountService;
import kr.ac.jbnu.se.tetris.FileRecordRepository;
import kr.ac.jbnu.se.tetris.FileUserRepository;
import kr.ac.jbnu.se.tetris.RecordRepository;

/** 같은 데이터 폴더를 쓰는 저장소 묶음. 앱 시작 때 한 번 만들어 생성자로 넘긴다. */
public record Repositories(AccountService accounts, RecordRepository records,
        KeyBindingRepository keys, SaveRepository saves) {

    public static Repositories at(Path baseDir) {
        return new Repositories(
                new AccountService(new FileUserRepository(baseDir)),
                new FileRecordRepository(baseDir),
                new FileKeyBindingRepository(baseDir),
                new FileSaveRepository(baseDir));
    }
}
