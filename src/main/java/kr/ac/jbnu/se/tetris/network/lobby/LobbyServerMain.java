package kr.ac.jbnu.se.tetris.network.lobby;

/**
 * 로비 전용 프로세스 진입점.
 * 
 * === 실행 방법 ===
 * 
 * 1. 로컬 테스트:
 *    java -jar lobby-server.jar
 *    java -jar lobby-server.jar 8765
 * 
 * 2. 환경변수로 포트 지정 (클라우드 배포용):
 *    set PORT=8765 && java -jar lobby-server.jar
 * 
 * 3. ngrok으로 외부 공개:
 *    먼저 서버 실행 후 → ngrok tcp 8765
 *    → 팀원에게 ngrok 주소 공유
 * 
 * @author 3조 네트워크 담당
 */
public final class LobbyServerMain {

	public static void main(String[] args) throws Exception {
		// 포트 결정: 인자 > 환경변수 > 기본값
		int port = getPort(args);
		
		System.out.println("========================================");
		System.out.println("   테트리스 로비 서버 (3조)");
		System.out.println("========================================");
		System.out.println("포트: " + port);
		System.out.println("최대 방 개수: " + LobbyConfig.MAX_ROOMS);
		System.out.println("방당 최대 인원: " + LobbyConfig.MAX_PLAYERS_PER_ROOM);
		System.out.println("----------------------------------------");
		
		LobbyServer server = new LobbyServer();
		server.start(port);
		
		System.out.println("");
		System.out.println("[팁] 원격 접속하려면:");
		System.out.println("  1. 같은 네트워크: 이 PC의 IP 주소 사용");
		System.out.println("  2. 다른 네트워크: ngrok tcp " + port + " 실행");
		System.out.println("");
		System.out.println("Ctrl+C 로 종료");
		System.out.println("========================================");
		
		// 서버가 종료될 때까지 대기
		Thread.currentThread().join();
	}
	
	/**
	 * 포트 번호 가져오기
	 * 우선순위: 명령줄 인자 > PORT 환경변수 > 기본값(8765)
	 */
	private static int getPort(String[] args) {
		// 1. 명령줄 인자 확인
		if (args.length >= 1) {
			try {
				return Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				System.out.println("[경고] 잘못된 포트 번호: " + args[0]);
			}
		}
		
		// 2. 환경변수 확인 (Railway, Render 등 클라우드에서 사용)
		String envPort = System.getenv("PORT");
		if (envPort != null && !envPort.isEmpty()) {
			try {
				return Integer.parseInt(envPort);
			} catch (NumberFormatException e) {
				System.out.println("[경고] 환경변수 PORT 값이 잘못됨: " + envPort);
			}
		}
		
		// 3. 기본값
		return LobbyConfig.DEFAULT_PORT;
	}

	private LobbyServerMain() {
	}
}
