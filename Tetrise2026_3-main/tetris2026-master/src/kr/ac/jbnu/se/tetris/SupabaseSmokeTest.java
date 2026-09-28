package kr.ac.jbnu.se.tetris;

import java.util.Arrays;

public class SupabaseSmokeTest {
    public static void main(String[] args) {
        SupabaseDatabase db = new SupabaseDatabase();

        System.out.println("[1] READY: " + db.isReady());
        if (!db.isReady()) {
            System.out.println("Supabase env not loaded. Check .env or environment variables.");
            return;
        }

        String email = "smoke_" + System.currentTimeMillis() + "@gmail.com";
        String password = "123456";

        System.out.println("[2] REGISTER: " + db.register(email, password));
        System.out.println("[3] LOGIN: " + db.login(email, password));

        if (db.login(email, password)) {
            System.out.println("[4] KEY SETTINGS SAVE: " + db.saveKeySettings(37, 39, 38, 40, 65, 68, 87, 83));
            System.out.println("[5] KEY SETTINGS LOAD: " + db.loadKeySettings());

            Tetrominoes[] boardData = new Tetrominoes[10 * 22];
            Arrays.fill(boardData, Tetrominoes.NoShape);
            boardData[0] = Tetrominoes.TShape;
            boardData[1] = Tetrominoes.LShape;
            boardData[2] = Tetrominoes.SquareShape;

            Shape piece = new Shape();
            piece.setShape(Tetrominoes.LineShape);

            System.out.println("[6] SAVE BOARD: " + db.saveBoard(boardData, piece, 3, 17, 2));
            System.out.println("[7] LOAD BOARD: " + db.loadBoard());
            System.out.println("[8] SAVE USER RECORD: " + db.saveUserRecord("smoke_user", 1, 1));
            System.out.println("[9] LOAD USER RECORD: " + db.loadUserRecord());
        } else {
            System.out.println("Login failed. Stopping after login check.");
        }
    }
}
