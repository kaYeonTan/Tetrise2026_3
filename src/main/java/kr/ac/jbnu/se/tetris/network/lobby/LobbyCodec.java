package kr.ac.jbnu.se.tetris.network.lobby;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class LobbyCodec {

	private static final Gson GSON = new GsonBuilder().create();

	private LobbyCodec() {
	}

	public static String encode(LobbyMessage msg) {
		return GSON.toJson(msg);
	}

	public static LobbyMessage decode(String line) {
		return GSON.fromJson(line, LobbyMessage.class);
	}
}
