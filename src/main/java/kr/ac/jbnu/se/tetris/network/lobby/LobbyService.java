package kr.ac.jbnu.se.tetris.network.lobby;

import java.io.IOException;
import java.util.function.Consumer;

public interface LobbyService {

	void setPushHandler(Consumer<LobbyMessage> handler);

	boolean isConnected();

	LobbyMessage fetchMyRoom() throws IOException;

	void sendPlayerAction(LobbyMessage req) throws IOException;

	void sendCoopSnapshot(LobbyMessage req) throws IOException;

	LobbyMessage requestCoopSave(LobbyMessage req) throws IOException;
}
