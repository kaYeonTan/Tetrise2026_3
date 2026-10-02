package kr.ac.jbnu.se.tetris.network.lobby;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/** TCP 한 줄 JSON — 요청 1건당 응답 1줄 (로비 MVP). */
public class LobbyClientConnection implements AutoCloseable {

	private final Socket socket;
	private final BufferedReader in;
	private final BufferedWriter out;

	public LobbyClientConnection(String host, int port) throws IOException {
		socket = new Socket(host, port);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
		out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
	}

	public synchronized LobbyMessage request(LobbyMessage req) throws IOException {
		String line = LobbyCodec.encode(req);
		out.write(line);
		out.newLine();
		out.flush();
		String respLine = in.readLine();
		if (respLine == null) {
			throw new IOException("서버 연결이 끊어졌어요.");
		}
		return LobbyCodec.decode(respLine);
	}

	@Override
	public void close() throws IOException {
		socket.close();
	}
}
