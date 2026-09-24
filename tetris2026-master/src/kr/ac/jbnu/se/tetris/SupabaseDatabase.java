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

	// 데이터베이스 생성
	public SupabaseDatabase() {
		String url = System.getenv("SUPABASE_URL");
		String key = System.getenv("SUPABASE_ANON_KEY");
		if ((url == null || url.isEmpty()) || (key == null || key.isEmpty())) {
			String[] env = loadDotEnv();
			if (url == null || url.isEmpty()) {
				url = env[0];
			}
			if (key == null || key.isEmpty()) {
				key = env[1];
			}
		}
		supabaseUrl = url;
		anonKey = key;
	}

	public SupabaseDatabase(String supabaseUrl, String anonKey) {
		this.supabaseUrl = supabaseUrl;
		this.anonKey = anonKey;
	}

	// .env 파일 읽기
	public String[] loadDotEnv() {
		String[] result = { "", "" };
		File envFile = new File(".env");
		if (!envFile.exists()) {
			return result;
		}
		try (FileInputStream fis = new FileInputStream(envFile);
				InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
				BufferedReader reader = new BufferedReader(isr)) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("#") || line.trim().isEmpty()) {
					continue;
				}
				int index = line.indexOf('=');
				if (index < 0) {
					continue;
				}
				String key = line.substring(0, index).trim();
				String value = line.substring(index + 1).trim();
				if ("SUPABASE_URL".equals(key)) {
					result[0] = value;
				} else if ("SUPABASE_ANON_KEY".equals(key)) {
					result[1] = value;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	// 로그인 정보 넣기
	public void setCredentials(String supabaseUrl, String anonKey) {
		this.supabaseUrl = supabaseUrl;
		this.anonKey = anonKey;
		this.accessToken = null;
		this.userId = null;
	}

	// 준비됐는지
	public boolean isReady() {
		if (supabaseUrl == null || anonKey == null) {
			return false;
		}
		if (supabaseUrl.isEmpty() || anonKey.isEmpty()) {
			return false;
		}
		return true;
	}

	// 주소 가져오기
	public String getSupabaseUrl() {
		return supabaseUrl;
	}

	public String getAnonKey() {
		return anonKey;
	}

	// 회원가입 호출
	public boolean register(String email, String password) {
		String body = "{\"email\":\"" + escape(email) + "\",\"password\":\"" + escape(password) + "\"}";
		String result = request("/auth/v1/signup", "POST", body, false);
		if (result.startsWith("{")) {
			if (!result.contains("\"error\"")) {
				if (!result.contains("\"msg\"")) {
					return true;
				}
			}
		}
		return false;
	}

	// 로그인 호출
	public boolean login(String email, String password) {
		String body = "{\"email\":\"" + escape(email) + "\",\"password\":\"" + escape(password) + "\"}";
		String result = request("/auth/v1/token?grant_type=password", "POST", body, false);
		accessToken = getJsonValue(result, "access_token");
		if (accessToken == null || accessToken.isEmpty()) {
			return false;
		}
		String temp = getJsonValue(result, "user");
		userId = getJsonValue(temp, "id");
		if (userId == null || userId.isEmpty()) {
			return false;
		}
		return true;
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

	// 통신용 함수
	public String request(String path, String method, String body, boolean loginRequired) {
		if (!isReady()) {
			return "";
		}
		if (loginRequired) {
			if (accessToken == null || userId == null) {
				return "";
			}
		}
		try {
			URL url = new URL(supabaseUrl + path);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(method);
			connection.setRequestProperty("apikey", anonKey);
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Prefer", "resolution=merge-duplicates,return=representation");
			if (accessToken != null) {
				connection.setRequestProperty("Authorization", "Bearer " + accessToken);
			}
			if (body != null) {
				connection.setDoOutput(true);
				OutputStream output = connection.getOutputStream();
				output.write(body.getBytes(StandardCharsets.UTF_8));
				output.close();
			}
			InputStream input = connection.getResponseCode() >= 400 ? connection.getErrorStream() : connection.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
			String result = "";
			String line;
			while ((line = reader.readLine()) != null) {
				result += line;
			}
			reader.close();
			connection.disconnect();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	// 보드 배열을 문자열로 바꾸기
	public String boardToString(Tetrominoes[] board) {
		String result = "";
		for (int i = 0; i < board.length; i++) {
			if (i > 0) {
				result += ",";
			}
			result += board[i].name();
		}
		return result;
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