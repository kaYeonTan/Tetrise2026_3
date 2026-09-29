package kr.ac.jbnu.se.tetris.network.lobby;

/**
 * 로비 전용 프로세스 진입점.
 * <p>
 * 실행: {@code mvn -q exec:java -Dexec.mainClass=kr.ac.jbnu.se.tetris.network.lobby.LobbyServerMain}
 */
public final class LobbyServerMain {

	public static void main(String[] args) throws Exception {
		int port = LobbyConfig.DEFAULT_PORT;
		if (args.length >= 1) {
			port = Integer.parseInt(args[0]);
		}
		LobbyServer server = new LobbyServer();
		server.start(port);
		System.out.println("[LobbyServer] Ctrl+C to stop.");
		Thread.currentThread().join();
	}

	private LobbyServerMain() {
	}
}
