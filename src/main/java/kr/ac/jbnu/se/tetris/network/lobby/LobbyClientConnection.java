package kr.ac.jbnu.se.tetris.network.lobby;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/** TCP 한 줄 JSON — 요청/응답 + 서버 푸시. */
public class LobbyClientConnection implements AutoCloseable {

	private static final Logger LOG = Logger.getLogger(LobbyClientConnection.class.getName());

	private final Socket socket;
	private final BufferedReader in;
	private final BufferedWriter out;
	private final BlockingQueue<LobbyMessage> inbound = new LinkedBlockingQueue<>();
	private volatile Consumer<LobbyMessage> pushHandler;
	private volatile boolean open = true;
	private final Thread reader;

	public LobbyClientConnection(String host, int port) throws IOException {
		socket = new Socket(host, port);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
		out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
		reader = new Thread(this::readLoop, "lobby-client-read");
		reader.setDaemon(true);
		reader.start();
	}

	public void setPushHandler(Consumer<LobbyMessage> handler) {
		this.pushHandler = handler;
	}

	public synchronized LobbyMessage request(LobbyMessage req) throws IOException {
		if (!open) {
			throw new IOException("연결이 닫혔어요.");
		}
		String line = LobbyCodec.encode(req);
		out.write(line);
		out.newLine();
		out.flush();
		while (open) {
			LobbyMessage resp;
			try {
				resp = inbound.poll(30, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new IOException("대기 중 중단됨");
			}
			if (resp == null) {
				throw new IOException("서버 응답 시간 초과");
			}
			if (LobbyPush.isPush(resp.type)) {
				dispatchPush(resp);
				continue;
			}
			return resp;
		}
		throw new IOException("서버 연결이 끊어졌어요.");
	}

	public synchronized void sendFireAndForget(LobbyMessage req) throws IOException {
		if (!open) {
			throw new IOException("연결이 닫혔어요.");
		}
		out.write(LobbyCodec.encode(req));
		out.newLine();
		out.flush();
	}

	private void readLoop() {
		try {
			String line;
			while (open && (line = in.readLine()) != null) {
				LobbyMessage msg = LobbyCodec.decode(line);
				if (msg == null) {
					continue;
				}
				if (LobbyPush.isPush(msg.type)) {
					dispatchPush(msg);
				} else {
					inbound.offer(msg);
				}
			}
		} catch (IOException ex) {
			LOG.log(Level.FINE, "read loop ended", ex);
		} finally {
			open = false;
		}
	}

	private void dispatchPush(LobbyMessage msg) {
		Consumer<LobbyMessage> h = pushHandler;
		if (h != null) {
			h.accept(msg);
		}
	}

	@Override
	public void close() throws IOException {
		open = false;
		socket.close();
	}
}
