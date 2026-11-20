package kr.ac.jbnu.se.tetris.repository;

import java.nio.file.Path;
import java.util.ArrayList;

import kr.ac.jbnu.se.tetris.KeyBindingsCodec;
import kr.ac.jbnu.se.tetris.KeySettings;

/** data/keys.txt 에 기존과 같은 형식으로 읽고 쓴다. */
public class FileKeyBindingRepository implements KeyBindingRepository {

    private static final String FILE_NAME = "keys.txt";

    private final TextFileStore store;

    public FileKeyBindingRepository(Path baseDir) {
        this.store = new TextFileStore(baseDir);
    }

    @Override
    public void saveKeys() {
        store.writeLines(store.dir() + FILE_NAME, new ArrayList<>(KeyBindingsCodec.encode(KeySettings.current())), false);
    }

    @Override
    public void loadKeys() {
        KeySettings keys = KeyBindingsCodec.decode(store.readLines(store.dir() + FILE_NAME));
        if (keys != null) {
            keys.applyToGame();
        }
    }
}
