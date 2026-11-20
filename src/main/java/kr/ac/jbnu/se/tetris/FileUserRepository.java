package kr.ac.jbnu.se.tetris;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import kr.ac.jbnu.se.tetris.repository.TextFileStore;

/** data/users.txt 에 기존과 같은 형식으로 읽고 쓴다. */
public class FileUserRepository implements UserRepository {

    private static final String FILE_NAME = "users.txt";

    private final TextFileStore store;

    public FileUserRepository(Path baseDir) {
        this.store = new TextFileStore(baseDir);
    }

    @Override
    public List<UserRecord> findAll() {
        List<UserRecord> users = new ArrayList<>();
        for (String line : store.readLines(store.dir() + FILE_NAME)) {
            UserRecord user = UserRecord.fromLine(line);
            if (user != null) {
                users.add(user);
            }
        }
        return users;
    }

    @Override
    public boolean add(UserRecord user) {
        ArrayList<String> lines = new ArrayList<>();
        lines.add(user.toLine());
        return store.writeLines(store.dir() + FILE_NAME, lines, true);
    }

    @Override
    public boolean replacePassword(String nickname, String storedPassword) {
        String path = store.dir() + FILE_NAME;
        ArrayList<String> rewritten = new ArrayList<>();
        boolean found = false;
        for (String line : store.readLines(path)) {
            UserRecord user = UserRecord.fromLine(line);
            if (user != null && user.nick().equalsIgnoreCase(nickname)) {
                rewritten.add(new UserRecord(user.nick(), user.email(), storedPassword).toLine());
                found = true;
            } else {
                // 읽을 수 없는 줄도 그대로 두어 다른 데이터를 잃지 않는다.
                rewritten.add(line);
            }
        }
        return found && store.writeLines(path, rewritten, false);
    }
}
