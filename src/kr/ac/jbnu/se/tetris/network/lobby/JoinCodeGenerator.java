package kr.ac.jbnu.se.tetris.network.lobby;

import java.security.SecureRandom;

/** 비밀방 입장 코드 — 서버만 생성 (클라이언트가 비밀번호 정하지 않음). */
public final class JoinCodeGenerator {

	private static final char[] ALPHANUM = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();
	private static final SecureRandom RNG = new SecureRandom();

	private JoinCodeGenerator() {
	}

	public static String newCode() {
		char[] buf = new char[LobbyConfig.JOIN_CODE_LENGTH];
		for (int i = 0; i < buf.length; i++) {
			buf[i] = ALPHANUM[RNG.nextInt(ALPHANUM.length)];
		}
		return new String(buf);
	}
}
