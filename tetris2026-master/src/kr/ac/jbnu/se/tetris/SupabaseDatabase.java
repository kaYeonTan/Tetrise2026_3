package kr.ac.jbnu.se.tetris;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class SupabaseDatabase {

	public String supabaseUrl;
	public String anonKey;
	public String accessToken;
	public String userId;

	public SupabaseDatabase() {
		String u = System.getenv("SUPABASE_URL");
		String k = System.getenv("SUPABASE_ANON_KEY");
		if (u == null) {
			u = "";
		}
		if (k == null) {
			k = "";
		}
		if (u.isEmpty()) {
			String[] a = loadDotEnv();
			if (a != null) {
				u = a[0];
			}
		}
		if (k.isEmpty()) {
			String[] a = loadDotEnv();
			if (a != null) {
				k = a[1];
			}
		}
		supabaseUrl = u;
		anonKey = k;
	}

	public SupabaseDatabase(String supabaseUrl, String anonKey) {
		this.supabaseUrl = supabaseUrl;
		this.anonKey = anonKey;
	}

	public String[] loadDotEnv() {
		String[] a = { "", "" };
		File f = new File(".env");
		if (f.exists()) {
			try (FileInputStream fis = new FileInputStream(f);
					InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
					BufferedReader r = new BufferedReader(isr)) {
				String line = "";
				while ((line = r.readLine()) != null) {
					if (!line.startsWith("#")) {
						if (!line.trim().isEmpty()) {
							int i = line.indexOf('=');
							if (i >= 0) {
								String n = line.substring(0, i).trim();
								String v = line.substring(i + 1).trim();
								if (n.equals("SUPABASE_URL")) {
									a[0] = v;
								} else {
									if (n.equals("SUPABASE_ANON_KEY")) {
										a[1] = v;
									}
								}
							}
						}
					}
				}
			} catch (Exception x) {
				System.out.println("env file read error");
			}
		}
		return a;
	}

	// 로그인 정보 넣기
	public void setCredentials(String supabaseUrl, String anonKey) {
		this.supabaseUrl = supabaseUrl;
		this.anonKey = anonKey;
		this.accessToken = null;
		this.userId = null;
	}

	public boolean isReady() {
		if (supabaseUrl != null) {
			if (anonKey != null) {
				if (!supabaseUrl.isEmpty()) {
					if (!anonKey.isEmpty()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	// 주소 가져오기
	public String getSupabaseUrl() {
		return supabaseUrl;
	}

	public String getAnonKey() {
		return anonKey;
	}

	public boolean register(String email, String password) {
		String b = "{\"email\":\"" + escape(email) + "\",\"password\":\"" + escape(password) + "\"}";
		String r = request("/auth/v1/signup", "POST", b, false);
		if (r != null) {
			if (r.startsWith("{")) {
				if (!r.contains("\"error\"")) {
					if (!r.contains("\"msg\"")) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean login(String email, String password) {
		String b = "{\"email\":\"" + escape(email) + "\",\"password\":\"" + escape(password) + "\"}";
		String r = request("/auth/v1/token?grant_type=password", "POST", b, false);
		accessToken = getJsonValue(r, "access_token");
		if (accessToken != null) {
			if (!accessToken.isEmpty()) {
				String u = getJsonValue(r, "user");
				userId = getJsonValue(u, "id");
				if (userId != null) {
					if (!userId.isEmpty()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	// 키 세팅 저장
	public boolean saveKeySettings(int p1Left, int p1Right, int p1Rotate, int p1Drop, int p2Left, int p2Right, int p2Rotate, int p2Drop) {
		String body = "{\"user_id\":\"" + userId + "\",\"p1_left\":" + p1Left + ",\"p1_right\":" + p1Right + ",\"p1_rotate\":" + p1Rotate + ",\"p1_drop\":" + p1Drop + ",\"p2_left\":" + p2Left + ",\"p2_right\":" + p2Right + ",\"p2_rotate\":" + p2Rotate + ",\"p2_drop\":" + p2Drop + "}";
		String result = request("/rest/v1/key_settings", "POST", body, true);
		if (result.startsWith("{")) {
			return true;
		}
		if (result.startsWith("[")) {
			return true;
		}
		return false;
	}

	// 키 세팅 불러오기
	public String loadKeySettings() {
		try {
			String filter = URLEncoder.encode(userId, "UTF-8");
			return request("/rest/v1/key_settings?user_id=eq." + filter + "&limit=1", "GET", null, true);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	// 보드 저장 호출
	public boolean saveBoard(Tetrominoes[] board, Shape currentPiece, int currentX, int currentY, int lines) {
		String body = "{\"user_id\":\"" + userId + "\",\"board_data\":\"" + escape(boardToString(board)) + "\",\"current_shape\":\"" + currentPiece.getShape().name() + "\",\"current_x\":" + currentX + ",\"current_y\":" + currentY + ",\"lines\":" + lines + "}";
		String result = request("/rest/v1/board_saves", "POST", body, true);
		if (result.startsWith("{")) {
			return true;
		}
		if (result.startsWith("[")) {
			return true;
		}
		return false;
	}

	// 보드 저장용 큰 덩어리
	public boolean saveGame(Board board) {
		String data = boardToString(board.board) + "|" + board.isTwoPlayer + "|" + board.p2Piece.getShape().name() + "|" + board.p2X + "|" + board.p2Y;
		String body = "{\"user_id\":\"" + userId + "\",\"board_data\":\"" + escape(data) + "\",\"current_shape\":\"" + board.curPiece.getShape().name() + "\",\"current_x\":" + board.curX + ",\"current_y\":" + board.curY + ",\"lines\":" + board.numLinesRemoved + "}";
		String result = request("/rest/v1/board_saves", "POST", body, true);
		if (result.startsWith("{")) {
			return true;
		}
		if (result.startsWith("[")) {
			return true;
		}
		return false;
	}

	// 보드 불러오기
	public boolean loadGame(Board board) {
		String result = loadBoard();
		String data = getJsonValue(result, "board_data");
		if (data == null || data.isEmpty()) {
			return false;
		}
		String[] values = data.split("\\|", -1);
		if (values.length < 1) {
			return false;
		}
		String[] savedBoard = values[0].split(",", -1);
		if (savedBoard.length != board.board.length) {
			return false;
		}
		try {
			for (int i = 0; i < savedBoard.length; i++) {
				board.board[i] = Tetrominoes.valueOf(savedBoard[i]);
			}
			board.curPiece.setShape(Tetrominoes.valueOf(getJsonValue(result, "current_shape")));
			board.curX = Integer.parseInt(getJsonValue(result, "current_x"));
			board.curY = Integer.parseInt(getJsonValue(result, "current_y"));
			board.numLinesRemoved = Integer.parseInt(getJsonValue(result, "lines"));
			if (values.length >= 5) {
				board.isTwoPlayer = Boolean.parseBoolean(values[1]);
				board.p2Piece.setShape(Tetrominoes.valueOf(values[2]));
				board.p2X = Integer.parseInt(values[3]);
				board.p2Y = Integer.parseInt(values[4]);
			}
			board.isStarted = true;
			board.timer.start();
			board.repaint();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// 사용자 기록 저장
	public boolean saveUserRecord(String nickname, int games, int wins) {
		String body = "{\"user_id\":\"" + userId + "\",\"nickname\":\"" + escape(nickname) + "\",\"games\":" + games + ",\"wins\":" + wins + "}";
		String result = request("/rest/v1/user_records", "POST", body, true);
		if (result.startsWith("{")) {
			return true;
		}
		if (result.startsWith("[")) {
			return true;
		}
		return false;
	}

	public String loadUserRecord() {
		try {
			String filter = URLEncoder.encode(userId, "UTF-8");
			return request("/rest/v1/user_records?user_id=eq." + filter + "&limit=1", "GET", null, true);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public String loadBoard() {
		try {
			String filter = URLEncoder.encode(userId, "UTF-8");
			return request("/rest/v1/board_saves?user_id=eq." + filter + "&order=saved_at.desc&limit=1", "GET", null, true);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public String request(String path, String method, String body, boolean loginRequired) {
		if (isReady()) {
			if (loginRequired) {
				if (accessToken != null) {
					if (userId != null) {
						return doRequest(path, method, body);
					}
				}
				return "";
			}
			return doRequest(path, method, body);
		}
		return "";
	}

	private String doRequest(String path, String method, String body) {
		try {
			URL u = new URL(supabaseUrl + path);
			HttpURLConnection c = (HttpURLConnection) u.openConnection();
			c.setRequestMethod(method);
			c.setRequestProperty("apikey", anonKey);
			c.setRequestProperty("Content-Type", "application/json");
			c.setRequestProperty("Prefer", "resolution=merge-duplicates,return=representation");
			if (accessToken != null) {
				c.setRequestProperty("Authorization", "Bearer " + accessToken);
			}
			if (body != null) {
				c.setDoOutput(true);
				OutputStream o = c.getOutputStream();
				o.write(body.getBytes(StandardCharsets.UTF_8));
				o.close();
			}
			InputStream in;
			if (c.getResponseCode() >= 400) {
				in = c.getErrorStream();
			} else {
				in = c.getInputStream();
			}
			BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
			String a = "";
			String line = "";
			while (line != null) {
				line = r.readLine();
				if (line != null) {
					a = a + line;
				}
			}
			r.close();
			c.disconnect();
			return a;
		} catch (Exception x) {
			System.out.println("supabase request error");
			return "";
		}
	}

	public String boardToString(Tetrominoes[] board) {
		String a = "";
		for (int i = 0; i < board.length; i++) {
			if (i > 0) {
				a = a + ",";
			}
			a = a + board[i].name();
		}
		return a;
	}

	public String escape(String value) {
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}

	public String getJsonValue(String json, String key) {
		if (json == null || json.isEmpty()) {
			return "";
		}
		String target = "\"" + key + "\"";
		int keyIndex = json.indexOf(target);
		if (keyIndex < 0) {
			return "";
		}
		int colonIndex = json.indexOf(':', keyIndex + target.length());
		if (colonIndex < 0) {
			return "";
		}
		int start = colonIndex + 1;
		while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
			start++;
		}
		if (start >= json.length()) {
			return "";
		}

		char first = json.charAt(start);
		if (first == '"') {
			int end = start + 1;
			while (end < json.length()) {
				if (json.charAt(end) == '\\') {
					end += 2;
					continue;
				}
				if (json.charAt(end) == '"') {
					break;
				}
				end++;
			}
			if (end >= json.length()) {
				return "";
			}
			return json.substring(start + 1, end).replace("\\\"", "\"").replace("\\\\", "\\");
		}
		if (first == '{' || first == '[') {
			int depth = 0;
			boolean inString = false;
			char prev = 0;
			for (int i = start; i < json.length(); i++) {
				char c = json.charAt(i);
				if (c == '"' && prev != '\\') {
					inString = !inString;
				}
				if (!inString) {
					if (c == '{' || c == '[') {
						depth++;
					} else if (c == '}' || c == ']') {
						depth--;
						if (depth == 0) {
							return json.substring(start, i + 1);
						}
					}
				}
				prev = c;
			}
			return json.substring(start);
		}

		int end = start;
		while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}') {
			end++;
		}
		return json.substring(start, end).trim();
	}
}